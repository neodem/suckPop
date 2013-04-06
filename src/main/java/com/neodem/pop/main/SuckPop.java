
package com.neodem.pop.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.neodem.pop.data.beans.AddressVO;
import com.neodem.pop.data.beans.ContentVO;
import com.neodem.pop.data.beans.MessageVO;
import com.neodem.pop.data.dao.PopDAO;
import com.neodem.pop.data.dao.impl.JDBCPopDAO;
import com.neodem.pop.data.util.Pop2Support;
import com.sun.mail.pop3.POP3Folder;

public class SuckPop {

	private static Log log = LogFactory.getLog(SuckPop.class.getName());

	// private static String SERVER = "pop.1and1.com";
	// private static String USERNAME = "m36707147-2";
	// private static String PASSWORD = "rk6Phone";

	private static String SERVER = "pop.gmail.com";

	private static String USERNAME = "trace2k@gmail.com";
//	private static String USERNAME = "recent:trace2k@gmail.com";

	private static String PASSWORD = "nd43589";

	private static int PORT = 995;

	private static final int BATCH = 100;

	private PopDAO dao;

	public SuckPop() {
		DataSource ds = Pop2Support.getDataSource();
		dao = new JDBCPopDAO(ds);
		// test();
		try {
			receive(SERVER, USERNAME, PASSWORD, PORT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new SuckPop();
	}

	/**
	 * "receive" method to fetch messages and process them.
	 */
	public void receive(String popServer, String popUser, String popPassword, int port) {
		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
		Store store = null;
		Folder folder = null;
		POP3Folder inbox = null;
		try {
			// -- Get hold of the default session --
			Properties props = System.getProperties();

			// add the ssl socket factory to the pop3 provider (only for gmail)
			props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
			props.setProperty("mail.pop3.socketFactory.fallback", "false");
			props.setProperty("mail.pop3.port", Integer.toString(port));
			props.setProperty("mail.pop3.socketFactory.port", Integer.toString(port));

			Session session = Session.getDefaultInstance(props, null);
			// -- Get hold of a POP3 message store, and connect to it --
			store = session.getStore("pop3");
			store.connect(popServer, popUser, popPassword);

			// -- Try to get hold of the default folder --
			folder = store.getDefaultFolder();
			if (folder == null)
				throw new Exception("No default folder");
			// -- ...and its INBOX --
			inbox = (POP3Folder) folder.getFolder("INBOX");
			if (inbox == null)
				throw new Exception("No POP3 INBOX");
			// -- Open the folder for read only --
			inbox.open(Folder.READ_ONLY);

			// fetch all the uids for the folder
			FetchProfile profile = new FetchProfile();
			profile.add(UIDFolder.FetchProfileItem.UID);
			Message[] messages = inbox.getMessages();
			inbox.fetch(messages, profile);
			for (int i = 0; i < messages.length; i++) {
				String uid = inbox.getUID(messages[i]);
				if (!dao.inDB(uid)) {
					MessageVO m = new MessageVO(inbox.getMessage(i + 1));
					m.setUid(uid);
					log.debug("storing message : " + uid);
					dao.storeMessage(m);
				}
				else {
					log.debug("message already stored : " + uid);
				}
			}

			log.info("complete");

		} catch (Exception ex) {
			log.error("exception : " + ex.getMessage(), ex);
			ex.printStackTrace();
		} finally {
			// -- Close down nicely --
			try {
				if (inbox != null)
					inbox.close(false);
				if (store != null)
					store.close();
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}
	}

	public void test() {
		ContentVO c = new ContentVO();
		c.setBody("body");
		c.setContentType("contentType");

		ContentVO c2 = new ContentVO();
		c2.setBody("body2");
		c2.setContentType("contentType2");

		List<ContentVO> contents = new ArrayList<ContentVO>();
		contents.add(c);
		contents.add(c2);

		AddressVO to = new AddressVO();
		to.setAddress("to:address");
		to.setPersonal("to:personal");

		List<AddressVO> tos = new ArrayList<AddressVO>();
		tos.add(to);

		MessageVO m = new MessageVO();
		m.setSubject("subject");
		m.setContents(contents);
		m.setFromName("from:personal");
		m.setFromEmailAddress("from:address");
		m.setTos(tos);
		m.setSentDate(new Date());

		dao.storeMessage(m);
	}
}
