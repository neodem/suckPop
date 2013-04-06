package com.neodem.pop.data.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.neodem.pop.data.beans.MessageVO;


public interface PopDAO {
	public String storeMessage(MessageVO message);

	public boolean inDB(String guid);

	/**
	 * 
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	public List<MessageVO> getMessages(int fromIndex, int toIndex); 
	
	public int getMessageCount();

	/**
	 * get count of messages with a given subject
	 * 
	 * @param subject
	 * @return
	 */
	public int getMessageCount(String subject);

	/**
	 * get a list of all message subjects
	 * @return
	 */
	public Set<String> getMessageSubjects();

	/**
	 * return a list of messages with a given subject from start to end indexes
	 * @param subject
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	public List<MessageVO> getMessages(String subject, int fromIndex, int toIndex);

}
