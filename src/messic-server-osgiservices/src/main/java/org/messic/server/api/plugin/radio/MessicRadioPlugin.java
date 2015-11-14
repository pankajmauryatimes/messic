package org.messic.server.api.plugin.radio;

import java.io.IOException;

import org.messic.server.api.plugin.MessicPlugin;

public interface MessicRadioPlugin
    extends MessicPlugin
{
    static final String MESSIC_RADIO_PLUGIN_NAME = "MessicRadio";

    void startCast()
        throws IOException;

    MessicRadioStatus getStatus();
}
