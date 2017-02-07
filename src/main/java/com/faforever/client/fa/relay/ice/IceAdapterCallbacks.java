package com.faforever.client.fa.relay.ice;

import com.faforever.client.fa.relay.GpgGameMessage;
import com.faforever.client.fa.relay.ice.event.GpgGameMessageEvent;
import com.faforever.client.fa.relay.ice.event.IceAdapterStateChanged;
import com.faforever.client.remote.FafService;
import com.google.common.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.List;

/**
 * Dispatches all methods that the ICE adapter can call on its client.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class IceAdapterCallbacks {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final EventBus eventBus;

  private final FafService fafService;

  @Inject
  public IceAdapterCallbacks(EventBus eventBus, FafService fafService) {
    this.eventBus = eventBus;
    this.fafService = fafService;
  }

  public void onNeedSdp(long localPlayerId, long remotePlayerId) {
    logger.debug("SDP for connection {}/{} requested", localPlayerId, remotePlayerId);
  }

  public void onConnectionStateChanged(String newState) {
    logger.debug("ICE adapter connection state changed: {}", newState);
    eventBus.post(new IceAdapterStateChanged(newState));
  }

  public void onGpgNetMessageReceived(String header, List<Object> chunks) {
    logger.debug("Message from preferences: {} {}", header, chunks);
    eventBus.post(new GpgGameMessageEvent(new GpgGameMessage(header, chunks)));
  }

  public void onSdpGathered(long localPlayerId, long remotePlayerId, String sdp) {
    logger.debug("Gathered SDP for connection {}/{}: {}", localPlayerId, remotePlayerId, sdp);
    fafService.sendSdp((int) remotePlayerId, sdp);
  }

  public void onPeerStateChanged(long localPlayerId, long remotePlayerId, String state) {
    logger.debug("Connection state for peer {} changed: {}", remotePlayerId, state);
  }
}
