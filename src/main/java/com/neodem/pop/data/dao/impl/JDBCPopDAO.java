
package com.neodem.pop.data.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.neodem.pop.data.beans.AddressVO;
import com.neodem.pop.data.beans.ContentVO;
import com.neodem.pop.data.beans.MessageVO;
import com.neodem.pop.data.dao.PopDAO;

/**
 * use JDBC to connect and save the data in one table
 * 
 * save: to from subject sent time message text
 * 
 * 
 * @author Vince
 * 
 */
public class JDBCPopDAO implements PopDAO {

	private static Log log = LogFactory.getLog(JDBCPopDAO.class.getName());

	private DataSource ds;
	private JdbcTemplate t;

	public JDBCPopDAO(DataSource ds) {
		this.ds = ds;
		t = new JdbcTemplate(ds);
	}

	/**
	 * will check guid
	 */
	public String storeMessage(MessageVO message) {
		String to = message.getFirstPrintableTo();
		String from = message.getPrintableFrom();
		String subject = message.getPrintableSubject();
		long sent = message.getSentTime();
		String content = message.getFirstPrintableContent(false);
		String guid = message.getUid();

		if (!StringUtils.isBlank(guid)) {
			if (inDB(guid)) {
				return null;
			}
		}

		saveRecord(guid, to, from, subject, sent, content);
		return guid;
	}

	public boolean inDB(String guid) {
		Connection con = null;
		Statement s = null;
		ResultSet rs = null;

		boolean inDB = false;

		try {
			con = ds.getConnection();
			s = con.createStatement();

			rs = s.executeQuery("SELECT * FROM messages WHERE `guid` = '" + guid + "'");

			if (rs.first()) {
				inDB = true;
			} else {
				inDB = false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (SQLException sqlex) {
					// ignore -- as we can't do anything about it here
				}

				s = null;
			}

			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqlex) {
					// ignore -- as we can't do anything about it here
				}

				con = null;
			}
		}

		return inDB;
	}

	private Long saveRecord(String guid, String to, String from, String subject, long sentTime, String content) {
		Connection conn = null;
		PreparedStatement ps = null;

		try {
			conn = ds.getConnection();

			StringBuffer b = new StringBuffer();
			b.append("INSERT INTO ");
			b.append("messages");
			b.append('(');
			if (guid != null) {
				b.append("guid");
				b.append(',');
			}
			b.append("fromAddress");
			b.append(',');
			b.append("toAddress");
			b.append(',');
			b.append("sentTime");
			b.append(',');
			b.append("subject");
			b.append(',');
			b.append("message");
			b.append(')');
			if (guid != null) {
				b.append(" VALUES (?,?,?,?,?,?)");
			} else {
				b.append(" VALUES (?,?,?,?,?)");
			}
			ps = conn.prepareStatement(b.toString());

			if (guid != null) {
				ps.setString(1, guid);
				ps.setString(2, from);
				ps.setString(3, to);
				ps.setLong(4, sentTime);
				ps.setString(5, subject);
				ps.setString(6, content);
			} else {
				ps.setString(1, from);
				ps.setString(2, to);
				ps.setLong(3, sentTime);
				ps.setString(4, subject);
				ps.setString(5, content);
			}

			ps.executeUpdate();

			log.debug("Storing : " + sentTime + ",'" + subject + "'");

			ps.close();
			ps = null;

			conn.close();
			conn = null;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			/*
			 * close any jdbc instances here that weren't explicitly closed
			 * during normal code path, so that we don't 'leak' resources...
			 */

			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException sqlex) {
					// ignore -- as we can't do anything about it here
				}

				ps = null;
			}

			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlex) {
					// ignore -- as we can't do anything about it here
				}

				conn = null;
			}
		}

		return null;
	}

	public int getMessageCount() {
		String sql = "SELECT count(*) FROM messages";
		return t.queryForInt(sql);
	}

	public List<MessageVO> getMessages(int fromIndex, int toIndex) {
		Connection con = null;
		Statement s = null;
		ResultSet rs = null;

		List<MessageVO> messages = new ArrayList<MessageVO>();

		try {
			con = ds.getConnection();
			s = con.createStatement();
			
			String sql = "SELECT * FROM messages ORDER BY sentTime LIMIT " + fromIndex + "," + (toIndex - fromIndex);

			rs = s.executeQuery(sql);

			while (rs.next()) {
				
				String from = rs.getString("fromAddress");
		        String to = rs.getString("toAddress");
		        Long time = new Long(rs.getLong("sentTime"));
		        String subject = rs.getString("subject");
		        String content = rs.getString("message");
		        
		        MessageVO message = new MessageVO();
		        message.setFromAddress(new AddressVO(from));
		        message.addTo(new AddressVO(to));
		        message.setSentDate(new Date(time.longValue()));
		        message.setSubject(subject);
		        
		        ContentVO c = new ContentVO();
				c.setBody(content);
				c.setContentType("text/plain");
		        
		        message.addContent(c);
		        
				messages.add(message);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (SQLException sqlex) {
					// ignore -- as we can't do anything about it here
				}

				s = null;
			}

			if (con != null) {
				try {
					con.close();
				} catch (SQLException sqlex) {
					// ignore -- as we can't do anything about it here
				}

				con = null;
			}
		}

		return messages;
	}

	public int getMessageCount(String subject) {
		String sql = "SELECT count(*) FROM messages where subject = ?";
		return t.queryForInt(sql, new Object[]{subject});
	}

	public Set<String> getMessageSubjects() {
		Collection c = t.query(
			    "select subject FROM messages",
			    new RowMapper() {
			        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			            return rs.getString("subject");
			        }
			    });
		
		Set<String> subjects = new HashSet<String>();
		subjects.addAll(c);
		
		return subjects;
	}

	public List<MessageVO> getMessages(String subject, int fromIndex, int toIndex) {
		String sql = "SELECT * FROM messages WHERE subject = ? ORDER BY sentTime LIMIT " + fromIndex + "," + (toIndex - fromIndex);
		
		//TODO ordering
		Collection c = t.query(
			   sql, new Object[]{subject},
			    new RowMapper() {
			        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
			        	MessageVO message = new MessageVO();
			        	 message.setFromAddress(new AddressVO(rs.getString("fromAddress")));
					        message.addTo(new AddressVO(rs.getString("toAddress")));
					        message.setSentDate(new Date(rs.getLong("sentTime")));
					        message.setSubject(rs.getString("subject"));
					        
					        ContentVO c = new ContentVO();
							c.setBody(rs.getString("message"));
							c.setContentType("text/plain");
					        
					        message.addContent(c);
			            return message;
			        }
			    });
		
		List<MessageVO> messages = new ArrayList<MessageVO>();
		messages.addAll(c);
		
		return messages;
	}
}
