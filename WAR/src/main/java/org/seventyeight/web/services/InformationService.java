package org.seventyeight.web.services;

import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.LocalSession;
import org.cometd.bayeux.server.ServerMessage;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.java.annotation.Listener;
import org.cometd.java.annotation.Service;
import org.cometd.java.annotation.Session;
import org.cometd.server.AbstractService;
import org.cometd.server.BayeuxServerImpl;

import javax.inject.Inject;

/**
 * @author cwolfgang
 *         Date: 10-12-12
 *         Time: 21:54
 */
@Service
public class InformationService {


    public void info( ServerSession remote, ServerMessage.Mutable message ) {
        System.out.println( "BBBBBBBBBBBBAAAAAAAAAAAAAAAAAAAAMMMMMMMMMMMMMMMMM!!!!" );
        BayeuxServer server = new BayeuxServerImpl();
        //server.
    }
}
