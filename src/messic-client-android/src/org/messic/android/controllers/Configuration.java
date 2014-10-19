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
package org.messic.android.controllers;

import org.messic.android.controllers.messicdiscovering.MessicServerInstance;
import org.messic.android.util.MessicPreferences;

import android.content.Context;

public class Configuration
{

    private static MessicServerInstance instance = null;

    private static String lastToken = null;

    public static String getLastToken()
    {
        return lastToken;
    }

    public static void setToken( String token )
    {
        lastToken = token;
    }

    public static String getBaseUrl()
    {
        MessicServerInstance instance = getCurrentMessicService();
        return ( instance.secured ? "https" : "http" ) + "://" + instance.ip + ":" + instance.port + "/messic";
    }

    public static void setMessicService( Context context, MessicServerInstance instance )
    {
        MessicPreferences mp = new MessicPreferences( context );
        mp.setLastMessicServerUsed( instance );
        Configuration.instance = instance;
    }

    public static MessicServerInstance getLastMessicServerUsed( Context context )
    {
        MessicPreferences p = new MessicPreferences( context );
        MessicServerInstance prefferedServer = p.getLastMessicServerUsed();
        instance = prefferedServer;
        return instance;
    }

    /**
     * Obtain the messic server instance used
     * 
     * @return {@link String}
     */
    public static MessicServerInstance getCurrentMessicService()
    {
        return instance;
    }

}
