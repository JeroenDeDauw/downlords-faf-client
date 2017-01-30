package com.faforever.client.api;

import com.faforever.client.api.dto.AchievementDefinition;
import com.faforever.client.api.dto.CoopLeaderboardEntry;
import com.faforever.client.api.dto.FeaturedMod;
import com.faforever.client.api.dto.FeaturedModFile;
import com.faforever.client.api.dto.History;
import com.faforever.client.api.dto.LeaderboardEntry;
import com.faforever.client.api.dto.Map;
import com.faforever.client.api.dto.PlayerAchievement;
import com.faforever.client.api.dto.PlayerEvent;
import com.faforever.client.api.dto.Ranked1v1Stats;
import com.faforever.client.api.dto.RatingType;
import com.faforever.client.api.dto.ReplayInfo;
import com.faforever.client.config.CacheNames;
import com.faforever.client.config.ClientProperties;
import com.faforever.client.config.ClientProperties.Api;
import com.faforever.client.coop.CoopMission;
import com.faforever.client.io.ByteCountListener;
import com.faforever.client.leaderboard.Ranked1v1EntryBean;
import com.faforever.client.map.MapBean;
import com.faforever.client.mod.FeaturedModBean;
import com.faforever.client.mod.Mod;
import com.faforever.client.replay.Replay;
import com.faforever.client.user.event.LoggedOutEvent;
import com.faforever.client.user.event.LoginSuccessEvent;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import static com.github.nocatch.NoCatch.noCatch;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Component
@Profile("!offline")
// TODO devide and conquer
public class FafApiAccessorImpl implements FafApiAccessor {

  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final EventBus eventBus;
  private final RestTemplateBuilder restTemplateBuilder;
  private final ClientProperties clientProperties;

  private CountDownLatch authorizedLatch;
  private RestOperations restOperations;

  @Inject
  public FafApiAccessorImpl(EventBus eventBus, RestTemplateBuilder restTemplateBuilder, ClientProperties clientProperties) {
    this.eventBus = eventBus;
    this.restTemplateBuilder = restTemplateBuilder;
    this.clientProperties = clientProperties;
    authorizedLatch = new CountDownLatch(1);
  }

  @PostConstruct
  void postConstruct() throws IOException {
    eventBus.register(this);
  }

  @Subscribe
  public void onLoggedOutEvent(LoggedOutEvent event) {
    authorizedLatch = new CountDownLatch(1);
  }

  @Subscribe
  public void onLoginSuccessEvent(LoginSuccessEvent event) {
    authorize(event.getUserId(), event.getUsername(), event.getPassword());
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<PlayerAchievement> getPlayerAchievements(int playerId) {
    logger.debug("Loading achievements for player: {}", playerId);
    return getPage("/data/players/" + playerId + "/achievements", 1);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<PlayerEvent> getPlayerEvents(int playerId) {
    logger.debug("Loading events for player: {}", playerId);
    return getPage("/data/players/" + playerId + "/events", 1);
  }

  @Override
  @SuppressWarnings("unchecked")
  @Cacheable(CacheNames.ACHIEVEMENTS)
  public List<AchievementDefinition> getAchievementDefinitions() {
    logger.debug("Loading achievement definitions");
    return getPage("/data/achievements?sort=order", 1);
  }

  @Override
  @Cacheable(CacheNames.ACHIEVEMENTS)
  public AchievementDefinition getAchievementDefinition(String achievementId) {
    logger.debug("Getting definition for achievement {}", achievementId);
    return getOne("/data/achievements/" + achievementId, AchievementDefinition.class);
  }

  @Override
  @SneakyThrows
  public void authorize(int playerId, String username, String password) {
    Api apiProperties = clientProperties.getApi();

    ResourceOwnerPasswordResourceDetails details = new ResourceOwnerPasswordResourceDetails();
    details.setClientId(apiProperties.getClientId());
    details.setClientSecret(apiProperties.getClientSecret());
    details.setClientAuthenticationScheme(AuthenticationScheme.header);
    details.setAccessTokenUri(apiProperties.getAccessTokenUri());
    details.setUsername(username);
    details.setPassword(password);

    restOperations = restTemplateBuilder.configure(new OAuth2RestTemplate(details));

    authorizedLatch.countDown();
  }

  @Override
  @Cacheable(CacheNames.MODS)
  public List<Mod> getMods() {
    logger.debug("Loading available mods");
    return this.<com.faforever.client.api.dto.Mod>getAll("/mods").stream()
        .map(Mod::fromModInfo)
        .collect(toList());
  }

  @Override
  @Cacheable(CacheNames.FEATURED_MODS)
  public List<FeaturedMod> getFeaturedMods() {
    logger.debug("Getting featured mods");
    return getAll("/data/featured_mods");
  }

  @Override
  public MapBean findMapByName(String mapId) {
    logger.debug("Searching map: {}", mapId);
    return MapBean.fromMap(getOne("/data/maps/" + mapId, Map.class));
  }

  @Override
  @Cacheable(CacheNames.LEADERBOARD)
  public List<Ranked1v1EntryBean> getLeaderboardEntries(RatingType ratingType) {
    return this.<LeaderboardEntry>getAll("/data/leaderboards/" + ratingType.getString()).stream()
        .map(Ranked1v1EntryBean::fromLeaderboardEntry)
        .collect(toList());
  }

  @Override
  public Ranked1v1Stats getRanked1v1Stats() {
    return getOne("/data/leaderboards/1v1/stats", Ranked1v1Stats.class);
  }

  @Override
  public Ranked1v1EntryBean getRanked1v1EntryForPlayer(int playerId) {
    return Ranked1v1EntryBean.fromLeaderboardEntry(getOne("/data/leaderboards/1v1/" + playerId, LeaderboardEntry.class));
  }

  @Override
  @Cacheable(CacheNames.RATING_HISTORY)
  public History getRatingHistory(RatingType ratingType, int playerId) {
    return getOne(format("/data/players/%d/ratings/%s/history", playerId, ratingType.getString()), History.class);
  }

  @Override
  @Cacheable(CacheNames.MAPS)
  public List<MapBean> getAllMaps() {
    logger.debug("Getting all maps");
    return getMaps("/data/maps");
  }

  @Override
  @Cacheable(CacheNames.MAPS)
  public List<MapBean> getMostDownloadedMaps(int count) {
    logger.debug("Getting most downloaded maps");
    return getMaps(format("/data/maps?page[size]=%d&sort=-downloads", count));
  }

  @Override
  @Cacheable(CacheNames.MAPS)
  public List<MapBean> getMostPlayedMaps(int count) {
    logger.debug("Getting most played maps");
    return getMaps(format("/data/maps?page[size]=%d&sort=-times_played", count));
  }

  @Override
  @Cacheable(CacheNames.MAPS)
  public List<MapBean> getBestRatedMaps(int count) {
    logger.debug("Getting most liked maps");
    return getMaps(format("/data/maps?page[size]=%d&sort=-rating", count));
  }

  @Override
  public List<MapBean> getNewestMaps(int count) {
    logger.debug("Getting most liked maps");
    return getMaps(format("/data/maps?page[size]=%d&sort=-create_time", count));
  }

  @Override
  public void uploadMod(Path file, ByteCountListener listener) {
    MultiValueMap<String, Object> multipartContent = createFileMultipart(file, listener);
    noCatch(() -> post("/data/mods/upload", multipartContent));
  }

  @Override
  public void uploadMap(Path file, boolean isRanked, ByteCountListener listener) throws IOException {
    // FIXME fix with #481
//    post("/maps/upload", multipartContent);
  }

  @Override
  public void changePassword(String username, String currentPasswordHash, String newPasswordHash) throws IOException {
    logger.debug("Changing password");

    HashMap<String, String> httpDict = new HashMap<>();
    // TODO this should not be necessary; we are oauthed so the server knows our username
    httpDict.put("name", username);
    httpDict.put("pw_hash_old", currentPasswordHash);
    httpDict.put("pw_hash_new", newPasswordHash);

    post("/users/change_password", httpDict);
  }

  @Override
  public Mod getMod(String uid) {
    return Mod.fromModInfo(getOne("/mods/" + uid, com.faforever.client.api.dto.Mod.class));
  }

  @Override
  @Cacheable(CacheNames.FEATURED_MOD_FILES)
  public List<FeaturedModFile> getFeaturedModFiles(FeaturedModBean featuredModBean, Integer version) {
    String innerVersion = version == null ? "latest" : String.valueOf(version);
    return getAll(format("/data/featured_mods/%s/files/%s", featuredModBean.getId(), innerVersion));
  }

  @Override
  public List<Replay> searchReplayByPlayer(String playerName) {
    return this.<ReplayInfo>getAll("/data/replay?filter[player]=" + playerName)
        .parallelStream().map(Replay::fromReplayInfo).collect(Collectors.toList());
  }

  @Override
  public List<Replay> searchReplayByMap(String mapName) {
    return this.<ReplayInfo>getAll("/data/replay?filter[map]=" + mapName)
        .parallelStream().map(Replay::fromReplayInfo).collect(Collectors.toList());
  }

  @Override
  public List<Replay> searchReplayByMod(FeaturedMod featuredMod) {
    return this.<ReplayInfo>getAll("/data/replay?filter[mod]=" + featuredMod.getId())
        .parallelStream().map(Replay::fromReplayInfo).collect(Collectors.toList());
  }

  @Override
  public List<Replay> getNewestReplays(int count) {
    return this.<ReplayInfo>getAll(format("/data/replay?page[size]=%d&sort=-date", count))
        .parallelStream().map(Replay::fromReplayInfo).collect(Collectors.toList());
  }

  @Override
  public List<Replay> getHighestRatedReplays(int count) {
    return this.<ReplayInfo>getAll(format("/data/replay?page[size]=%d&sort=-rating", count))
        .parallelStream().map(Replay::fromReplayInfo).collect(Collectors.toList());
  }

  @Override
  public List<Replay> getMostWatchedReplays(int count) {
    return this.<ReplayInfo>getAll(format("/data/replay?page[size]=%d&sort=-plays", count))
        .parallelStream().map(Replay::fromReplayInfo).collect(Collectors.toList());
  }

  @Override
  @Cacheable(CacheNames.COOP_MAPS)
  public List<CoopMission> getCoopMissions() {
    logger.debug("Loading available coop missions");
    return this.<com.faforever.client.api.dto.CoopMission>getAll("/data/coop/missions")
        .stream().map(CoopMission::fromCoopInfo).collect(toList());
  }

  @Override
  @Cacheable(CacheNames.COOP_LEADERBOARD)
  public List<CoopLeaderboardEntry> getCoopLeaderboard(String missionId, int numberOfPlayers) {
    return this.getAll(String.format("/data/coop/leaderboards/%s/%d?page[size]=100", missionId, numberOfPlayers));
  }

  @NotNull
  private MultiValueMap<String, Object> createFileMultipart(Path file, ByteCountListener listener) {
    MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
    form.add("file", new FileSystemResource(file.toFile()));
    return form;
  }

  private void post(String endpointPath, Object content) throws IOException {
    // FIXME fix with #481
  }

  private List<MapBean> getMaps(String query) {
    logger.debug("Loading available maps");
    return this.<Map>getAll(query)
        .stream()
        .map(MapBean::fromMap)
        .collect(toList());
  }

  @SuppressWarnings("unchecked")
  @SneakyThrows
  private <T> T getOne(String endpointPath, Class<T> type) {
    authorizedLatch.await();
    return restOperations.getForObject(endpointPath, type);
  }

  @SuppressWarnings("unchecked")
  @SneakyThrows
  private <T> List<T> getPage(String endpointPath, int page) {
    authorizedLatch.await();
    return restOperations.getForObject(endpointPath, List.class, ImmutableMap.of(
        "page[number]", page
    ));
  }

  private <T> List<T> getAll(String endpointPath) {
    return getAll(endpointPath, Integer.MAX_VALUE);
  }

  @SneakyThrows
  private <T> List<T> getAll(String endpointPath, int count) {
    List<T> result = new LinkedList<>();
    List<T> current = null;
    int page = 1;
    while ((current == null || !current.isEmpty()) && result.size() < count) {
      current = getPage(endpointPath, page++);
      result.addAll(current);
    }
    return result;
  }
}
