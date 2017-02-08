package com.faforever.client.clan;

import com.faforever.client.remote.FafService;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;

@Lazy
@Service
public class ClanServiceImpl implements ClanService {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  public FafService fafService;
  private List<Clan> clans;
  private HashMap<String, Clan> clansByTag = new HashMap<String, Clan>();
  private Thread run;
  @Inject
  public ClanServiceImpl(FafService fafService) {
    this.fafService = fafService;
  }


  @Override
  public HashMap<String, Clan> getclansByTag() {
    clans = fafService.getClans();
    for (Clan clan : clans) {
      clansByTag.put(clan.getClanTag(), clan);
    }
    return clansByTag;
  }

  @Override
  public void setClansByTag(HashMap clansByTag) {
    this.clansByTag = clansByTag;
  }

  @Override
  public Clan getClanByTag(@Nullable String tag) {
    if (clansByTag.containsKey(tag)) {
      return clansByTag.get(tag);
    } else {
      logger.warn("Clan with tag: {} not found", tag);
    }
    return null;
  }
}
