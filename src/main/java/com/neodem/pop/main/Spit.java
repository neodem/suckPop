
package com.neodem.pop.main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.neodem.pop.data.beans.MessageVO;
import com.neodem.pop.data.dao.PopDAO;
import com.neodem.pop.data.dao.impl.JDBCPopDAO;
import com.neodem.pop.data.util.Pop2Support;

public class Spit {

	private static final int CHUNKSIZE = 10;

	private static final String FILENAME = "out.txt";

	private static Log log = LogFactory.getLog(Spit.class.getName());

	private PopDAO dao;

	public Spit() {
		DataSource ds = Pop2Support.getDataSource();
		dao = new JDBCPopDAO(ds);
		try {
			printByCatSequential();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printSequential() throws IOException {
		File outFile = new File(FILENAME);
		FileWriter out = new FileWriter(outFile);

		int count = dao.getMessageCount();
		int start = 0;
		int end = CHUNKSIZE - 1;
		int iterations = count / CHUNKSIZE + 1;

		for (int i = 0; i < iterations; i++) {
			List<MessageVO> messages = dao.getMessages(start, end);
			for (MessageVO message : messages) {
				String messageText = message.makePrintableMessage(true);
				out.write(messageText);
				out.write('\n');
				out.write('\n');
			}
			start = start + CHUNKSIZE;
			end = start + CHUNKSIZE - 1;
		}

		out.close();
	}

	private void printByCatSequential() throws IOException {
		File outFile = new File(FILENAME);
		FileWriter out = new FileWriter(outFile);

		Collection<String> subjects = dao.getMessageSubjects();
		for (String subject : subjects) {
			int count = dao.getMessageCount(subject);
			int start = 0;
			int end = CHUNKSIZE - 1;
			int iterations = count / CHUNKSIZE + 1;

			for (int i = 0; i < iterations; i++) {
				List<MessageVO> messages = dao.getMessages(subject, start, end);
				for (MessageVO message : messages) {
					String messageText = message.makePrintableMessage(true);
					out.write(messageText);
					out.write('\n');
					out.write('\n');
				}
				start = start + CHUNKSIZE;
				end = start + CHUNKSIZE - 1;
			}
		}

		out.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Spit();

	}

}
