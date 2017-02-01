package com.faforever.client.clan;

public interface ClanService {

  Clan getClanByTag(String tag);

  String getUrlOfClanWebsite(Clan clan);
}
