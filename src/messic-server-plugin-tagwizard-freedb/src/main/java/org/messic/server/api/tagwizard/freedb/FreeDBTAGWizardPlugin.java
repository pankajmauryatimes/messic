/*
 * Copyright (C) 2013
 *
 *  This file is part of Messic.
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.messic.server.api.tagwizard.freedb;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.messic.server.api.tagwizard.service.Album;
import org.messic.server.api.tagwizard.service.Song;
import org.messic.server.api.tagwizard.service.TAGWizardPlugin;

public class FreeDBTAGWizardPlugin
    implements TAGWizardPlugin
{
    private Logger log = Logger.getLogger( FreeDBTAGWizardPlugin.class );

    public static final String NAME = "FreeDB";

    public static final String DESCRIPTION =
        "TAGWIZARD plugin, based on audiotagger library to obtain mp3 tags (and other formarts) from music files";

    public static final float VERSION = 1.0f;

    public static final float MINIMUM_MESSIC_VERSION = 1.0f;

    /** configuration for the plugin */
    private Properties configuration;

    @Override
    public float getVersion()
    {
        return VERSION;
    }

    @Override
    public float getMinimumMessicVersion()
    {
        return MINIMUM_MESSIC_VERSION;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public String getDescription( Locale locale )
    {
        return DESCRIPTION;
    }

    @Override
    public Properties getConfiguration()
    {
        return this.configuration;
    }

    @Override
    public void setConfiguration( Properties properties )
    {
        this.configuration = properties;
    }

    protected Proxy getProxy()
    {
        if ( this.configuration != null )
        {
            String url = (String) this.configuration.get( "proxy-url" );
            String port = (String) this.configuration.get( "proxy-port" );
            if ( url != null && port != null && url.length() > 0 && port.length() > 0 )
            {
                SocketAddress addr = new InetSocketAddress( url, Integer.valueOf( port ) );
                Proxy proxy = new Proxy( Proxy.Type.HTTP, addr );
                return proxy;
            }
        }
        return null;
    }

    @Override
    public List<Album> getAlbumInfo( Album albumHelpInfo, File[] files )
    {
        if ( albumHelpInfo == null
            || ( albumHelpInfo.name == null && albumHelpInfo.author == null )
            || ( ( albumHelpInfo.name != null && albumHelpInfo.name.length() <= 0 ) && ( albumHelpInfo.author != null && albumHelpInfo.author.length() <= 0 ) ) )
        {
            return new ArrayList<Album>();
        }

        try
        {
            String baseURL =
                "http://freedb2.org/~cddb/cddb.cgi?cmd=cddb+album+"
                    + ( albumHelpInfo.author != null ? URLEncoder.encode( albumHelpInfo.author, "UTF-8" ) : "" )
                    + "+/+" + ( albumHelpInfo.name != null ? URLEncoder.encode( albumHelpInfo.name, "UTF-8" ) : "" )
                    + "&hello=messic+www.messic.org+Messic+1.0&proto=6";

            URL url = new URL( baseURL );
            Proxy proxy = getProxy();
            URLConnection uc = ( proxy != null ? url.openConnection( proxy ) : url.openConnection() );
            uc.setRequestProperty( "User-Agent", "Messic/1.0 +http://spheras.github.io/messic/" );

            String content = new String( readInputStream( uc.getInputStream() ), "UTF8" );
            if ( content.startsWith( "211" ) )
            {
                // found!
                String[] lines = content.split( "\r\n" );
                ArrayList<Album> result = new ArrayList<Album>();
                for ( int i = 1; i < lines.length; i++ )
                {
                    String[] words = lines[i].split( " " );
                    if ( words.length > 1 )
                    {
                        Album album = getAlbum( words[0], words[1] );
                        result.add( album );
                    }
                }
                return result;
            }
        }
        catch ( Exception e )
        {
            log.error( "failed!", e );
        }

        return null;
    }

    private Album getAlbum( String genre, String cdid )
    {
        String baseURL = "http://freedb2.org/freedb/" + genre + "/" + cdid;
        try
        {
            URL url = new URL( baseURL );
            Proxy proxy = getProxy();
            URLConnection uc = ( proxy != null ? url.openConnection( proxy ) : url.openConnection() );

            Album album = new Album();
            Properties p = new Properties();
            p.load( new StringReader( new String( readInputStream( uc.getInputStream() ), "UTF8" ) ) );
            p.load( uc.getInputStream() );

            String[] authortitle = p.getProperty( "DTITLE" ).split( "/" );
            album.author = authortitle[0].trim();
            album.name = authortitle[1].trim();
            Integer year = Integer.valueOf( p.getProperty( "DYEAR" ) );
            album.year = ( year != null ? year : 0 );
            album.genre = p.getProperty( "DGENRE" );
            album.songs = new ArrayList<Song>();
            album.comments = "Info obtained from FreeDB provider. http://www.freedb.org/";
            String songname = p.getProperty( "TTITLE0" );
            int track = 0;
            while ( songname != null )
            {
                Song song = new Song();
                song.track = track + 1;
                song.name = songname;
                album.songs.add( song );
                track++;
                songname = p.getProperty( "TTITLE" + track );
            }
            return album;
        }
        catch ( Exception e )
        {

        }

        return null;
    }

    /**
     * Read an inputstream and return a byte[] with the whole content of the readed at the inputstream
     * 
     * @param is {@link InputStream}
     * @return byte[] content
     * @throws IOException
     */
    public static byte[] readInputStream( InputStream is )
        throws IOException
    {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int cant = is.read( buffer );
        while ( cant > 0 )
        {
            baos.write( buffer, 0, cant );
            cant = is.read( buffer );
        }

        return baos.toByteArray();
    }

}
