package net.meano.ls;


import java.util.logging.Filter;

import java.util.logging.LogRecord;


public class CommandFilter
implements Filter
{
	Filter prevFilter = null;

	
	public boolean isLoggable(LogRecord record)
	{
		String message = record.getMessage();
		
		if ((message.contains("/login")) || (message.contains("/register")) || (message.contains("/changepass")) || (message.contains("/logout")))
			return false;
		if (this.prevFilter == null) {
			return true;
			}
		
		return this.prevFilter.isLoggable(record);
		}
	
}

