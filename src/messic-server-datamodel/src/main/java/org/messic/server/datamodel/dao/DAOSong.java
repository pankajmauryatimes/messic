package org.messic.server.datamodel.dao;

import java.util.List;

import org.messic.server.datamodel.MDOSong;



/**
 * DAO for Song table
 */
public interface DAOSong extends DAO<MDOSong>
{
	/**
	 * Find songs by a generic search. It searchs everything by songs, authors, etc..
	 * @param username {@link String} user scope
	 * @param List<String/> searches a list of contents related with songs 
	 * @return {@link List}<MDOSong/> List of songs that match to the search
	 */
    List<MDOSong> genericFind(String username, List<String> searches);
	
}
