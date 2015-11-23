package org.messic.server.facade.controllers.rest;

import org.jsondoc.core.annotation.Api;
import org.jsondoc.core.annotation.ApiError;
import org.jsondoc.core.annotation.ApiErrors;
import org.jsondoc.core.annotation.ApiMethod;
import org.jsondoc.core.annotation.ApiParam;
import org.jsondoc.core.annotation.ApiResponseObject;
import org.jsondoc.core.pojo.ApiParamType;
import org.jsondoc.core.pojo.ApiVerb;
import org.messic.server.api.APIRadio;
import org.messic.server.api.datamodel.User;
import org.messic.server.api.plugin.radio.MessicRadioInfo;
import org.messic.server.api.plugin.radio.MessicRadioStatus;
import org.messic.server.facade.controllers.rest.exceptions.NotAuthorizedMessicRESTException;
import org.messic.server.facade.controllers.rest.exceptions.UnknownMessicRESTException;
import org.messic.server.facade.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping( "/radio" )
@Api( name = "Search services", description = "Methods for search things" )
public class RadioController
{
    @Autowired
    public APIRadio radioAPI;

    @ApiMethod( path = "/services/radio/start", verb = ApiVerb.PUT, description = "Start the messic radio cast", produces = {
        MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE } )
    @ApiErrors( apierrors = { @ApiError( code = UnknownMessicRESTException.VALUE, description = "Unknown error" ),
        @ApiError( code = NotAuthorizedMessicRESTException.VALUE, description = "Forbidden access" ) } )
    @RequestMapping( value = "/start", method = RequestMethod.PUT )
    @ResponseStatus( HttpStatus.OK )
    @ResponseBody
    @ApiResponseObject
    public String start()
        throws NotAuthorizedMessicRESTException, UnknownMessicRESTException
    {

        User user = SecurityUtil.getCurrentUser();
        try
        {
            radioAPI.startRadio();
            return radioAPI.getInfo().radioURL;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new UnknownMessicRESTException( e );
        }

    }

    @ApiMethod( path = "/services/radio/stop", verb = ApiVerb.PUT, description = "Start the messic radio cast", produces = {
        MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE } )
    @ApiErrors( apierrors = { @ApiError( code = UnknownMessicRESTException.VALUE, description = "Unknown error" ),
        @ApiError( code = NotAuthorizedMessicRESTException.VALUE, description = "Forbidden access" ) } )
    @RequestMapping( value = "/stop", method = RequestMethod.PUT )
    @ResponseStatus( HttpStatus.OK )
    @ResponseBody
    @ApiResponseObject
    public void stop()
        throws NotAuthorizedMessicRESTException, UnknownMessicRESTException
    {

        User user = SecurityUtil.getCurrentUser();
        try
        {
            radioAPI.stopRadio();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new UnknownMessicRESTException( e );
        }
    }

    @ApiMethod( path = "/services/radio/{songSid}?songQueuePosition={songQueuePosition}", verb = ApiVerb.POST, description = "Cast the song with sid sidSong", produces = {
        MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE } )
    @ApiErrors( apierrors = { @ApiError( code = UnknownMessicRESTException.VALUE, description = "Unknown error" ),
        @ApiError( code = NotAuthorizedMessicRESTException.VALUE, description = "Forbidden access" ) } )
    @RequestMapping( value = "/{songSid}", method = RequestMethod.POST )
    @ResponseStatus( HttpStatus.OK )
    @ResponseBody
    @ApiResponseObject
    public void setSong( @ApiParam( name = "songSid", description = "SID of the album resource we want to play", paramType = ApiParamType.PATH, required = true ) @PathVariable Long songSid,
                         @ApiParam( name = "songQueuePosition", description = "Index Position of the song in the client queue", paramType = ApiParamType.QUERY, required = true ) @PathVariable Integer songQueuePosition )
        throws NotAuthorizedMessicRESTException, UnknownMessicRESTException
    {

        User user = SecurityUtil.getCurrentUser();
        try
        {
            radioAPI.playSong( user.getLogin(), songSid, songQueuePosition );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new UnknownMessicRESTException( e );
        }
    }

    @ApiMethod( path = "/services/radio/status", verb = ApiVerb.GET, description = "get the status of the radio service", produces = {
        MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE } )
    @ApiErrors( apierrors = { @ApiError( code = UnknownMessicRESTException.VALUE, description = "Unknown error" ),
        @ApiError( code = NotAuthorizedMessicRESTException.VALUE, description = "Forbidden access" ) } )
    @RequestMapping( value = "/status", method = RequestMethod.GET )
    @ResponseStatus( HttpStatus.OK )
    @ResponseBody
    @ApiResponseObject
    public MessicRadioInfo getStatus()
        throws NotAuthorizedMessicRESTException, UnknownMessicRESTException
    {

        User user = SecurityUtil.getCurrentUser();
        try
        {
            return radioAPI.getInfo();
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            throw new UnknownMessicRESTException( e );
        }
    }

}
