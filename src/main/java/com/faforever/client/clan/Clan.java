package com.faforever.client.clan;

import com.google.api.client.util.Key;
import lombok.Data;

@Data
public class Clan {

  @Key("clan_desc")
  private String description;
  @Key("clan_founder_id")
  private Integer clanFounderId;
  @Key("clan_id")
  private String clanId;
  @Key("clan_leader_id")
  private Integer clanLeader;
  @Key("clan_members")
  private Integer clanMembers;
  @Key("clan_name")
  private String clanName;
  @Key("clan_tag")
  private String clanTag;
  @Key("create_date")
  private String createTime;
  @Key("founder_name")
  private String founderName;
  @Key("leader_name")
  private String leaderName;
  @Key
  private Integer status;

  @Override
  public int hashCode() {
    return clanId != null ? clanId.hashCode() : 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Clan that = (Clan) o;

    return !(clanId != null ? !clanId.equals(that.clanId) : that.clanId != null);

  }
}