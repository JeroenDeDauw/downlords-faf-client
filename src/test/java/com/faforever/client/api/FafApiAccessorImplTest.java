package com.faforever.client.api;

import com.faforever.client.api.dto.AchievementDefinition;
import com.faforever.client.api.dto.History;
import com.faforever.client.api.dto.PlayerAchievement;
import com.faforever.client.api.dto.PlayerEvent;
import com.faforever.client.api.dto.Ranked1v1Stats;
import com.faforever.client.api.dto.RatingType;
import com.faforever.client.config.ClientProperties;
import com.faforever.client.leaderboard.Ranked1v1EntryBean;
import com.faforever.client.mod.Mod;
import com.faforever.client.mod.ModInfoBeanBuilder;
import com.faforever.client.preferences.PreferencesService;
import com.faforever.client.user.UserService;
import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FafApiAccessorImplTest {

  @Rule
  public TemporaryFolder preferencesDirectory = new TemporaryFolder();

  private FafApiAccessorImpl instance;

  @Mock
  private PreferencesService preferencesService;
  @Mock
  private UserService userService;
  @Mock
  private EventBus eventBus;
  @Mock
  private ClientProperties clientProperties;
  @Mock
  private OAuth2RestTemplate restTemplate;
  @Mock
  private RestTemplateBuilder restTemplateBuilder;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(preferencesService.getPreferencesDirectory()).thenReturn(preferencesDirectory.getRoot().toPath());
    when(userService.getUserId()).thenReturn(123);
    when(userService.getUsername()).thenReturn("junit");
    when(userService.getPassword()).thenReturn("42");

    when(restTemplateBuilder.configure(new OAuth2RestTemplate(any()))).thenReturn(restTemplate);

    instance = new FafApiAccessorImpl(eventBus, restTemplateBuilder, clientProperties);
    instance.postConstruct();
  }

  @Test
  public void testGetPlayerAchievements() throws Exception {
    PlayerAchievement playerAchievement1 = new PlayerAchievement();
    playerAchievement1.setId("1");
    playerAchievement1.setAchievementId("1-2-3");
    PlayerAchievement playerAchievement2 = new PlayerAchievement();
    playerAchievement2.setId("2");
    playerAchievement2.setAchievementId("2-3-4");
    List<PlayerAchievement> result = Arrays.asList(playerAchievement1, playerAchievement2);

    when(restTemplate.getForObject("http://api.example.com/players/123/achievements?page%5Bnumber%5D=1", List.class))
        .thenReturn(result);

    assertThat(instance.getPlayerAchievements(123), is(result));
  }

  @Test
  public void testGetAchievementDefinitions() throws Exception {
    AchievementDefinition achievementDefinition1 = new AchievementDefinition();
    achievementDefinition1.setId("1-2-3");
    AchievementDefinition achievementDefinition2 = new AchievementDefinition();
    achievementDefinition2.setId("2-3-4");
    List<AchievementDefinition> result = Arrays.asList(achievementDefinition1, achievementDefinition2);

    when(restTemplate.getForObject("http://api.example.com/achievements?sort=order&page%5Bnumber%5D=1", List.class))
        .thenReturn(result);

    assertThat(instance.getAchievementDefinitions(), is(result));
  }

  @Test
  public void testGetAchievementDefinition() throws Exception {
    AchievementDefinition achievementDefinition = new AchievementDefinition();
    achievementDefinition.setId("1-2-3");

    when(restTemplate.getForObject("http://api.example.com/achievements/123", AchievementDefinition.class))
        .thenReturn(achievementDefinition);

    assertThat(instance.getAchievementDefinition("123"), is(achievementDefinition));
  }

  @Test
  public void testGetPlayerEvents() throws Exception {
    PlayerEvent playerEvent1 = new PlayerEvent();
    playerEvent1.setId("1");
    playerEvent1.setEventId("1-1-1");
    playerEvent1.setCount(11);
    PlayerEvent playerEvent2 = new PlayerEvent();
    playerEvent2.setId("2");
    playerEvent2.setEventId("2-2-2");
    playerEvent2.setCount(22);
    List<PlayerEvent> result = Arrays.asList(playerEvent1, playerEvent2);

    when(restTemplate.getForObject("http://api.example.com/players/123/events?page%5Bnumber%5D=1", List.class))
        .thenReturn(result);

    assertThat(instance.getPlayerEvents(123), is(result));
  }

  @Test
  public void testGetMods() throws Exception {
    List<Mod> result = Arrays.asList(
        ModInfoBeanBuilder.create().defaultValues().uid("1").get(),
        ModInfoBeanBuilder.create().defaultValues().uid("2").get()
    );

    when(restTemplate.getForObject("http://api.example.com/mods?page%5Bnumber%5D=1", List.class))
        .thenReturn(result);

    when(restTemplate.getForObject("http://api.example.com/mods?page%5Bnumber%5D=2", List.class))
        .thenReturn(Collections.emptyList());

    assertThat(instance.getMods(), equalTo(result));

    verify(restTemplate).getForObject("http://api.example.com/mods?page%5Bnumber%5D=1", List.class);
    verify(restTemplate).getForObject("http://api.example.com/mods?page%5Bnumber%5D=2", List.class);
  }

  @Test
  public void testGetRanked1v1Entries() throws Exception {
    List<Ranked1v1EntryBean> result = Arrays.asList(
        Ranked1v1EntryBeanBuilder.create().defaultValues().username("user1").get(),
        Ranked1v1EntryBeanBuilder.create().defaultValues().username("user2").get()
    );

    when(restTemplate.getForObject("http://api.example.com/leaderboards/1v1?page%5Bnumber%5D=1", List.class))
        .thenReturn(result);
    when(restTemplate.getForObject("http://api.example.com/leaderboards/1v1?page%5Bnumber%5D=2", List.class))
        .thenReturn(Collections.emptyList());

    assertThat(instance.getLeaderboardEntries(RatingType.LADDER_1V1), equalTo(result));

    verify(restTemplate).getForObject("http://api.example.com/leaderboards/1v1?page%5Bnumber%5D=1", List.class);
    verify(restTemplate).getForObject("http://api.example.com/leaderboards/1v1?page%5Bnumber%5D=2", List.class);
  }

  @Test
  public void testGetRanked1v1Stats() throws Exception {
    Ranked1v1Stats ranked1v1Stats = new Ranked1v1Stats();
    ranked1v1Stats.setId("/leaderboards/1v1/stats");

    when(restTemplate.getForObject("http://api.example.com/leaderboards/1v1/stats", Ranked1v1Stats.class))
        .thenReturn(ranked1v1Stats);

    assertThat(instance.getRanked1v1Stats(), equalTo(ranked1v1Stats));
  }

  @Test
  public void testGetRanked1v1EntryForPlayer() throws Exception {
    // TODO Bean is definitely wrong here
    Ranked1v1EntryBean entry = Ranked1v1EntryBeanBuilder.create().defaultValues().username("user1").get();

    when(restTemplate.getForObject("http://api.example.com/leaderboards/1v1/123", Ranked1v1EntryBean.class))
        .thenReturn(entry);

    assertThat(instance.getRanked1v1EntryForPlayer(123), equalTo(entry));
  }

  @Test
  public void testGetRatingHistoryGlobal() throws Exception {
    History ratingHistory = instance.getRatingHistory(RatingType.GLOBAL, 123);

    when(restTemplate.getForObject("http://api.example.com/players/123/ratings/global/history", History.class))
        .thenReturn(ratingHistory);

    assertThat(ratingHistory.getData().values(), hasSize(3));
    assertThat(ratingHistory.getData().get("1469921413").get(0), is(1026.62f));
    assertThat(ratingHistory.getData().get("1469921413").get(1), is(49.4094f));
  }

  @Test
  public void testGetRatingHistory1v1() throws Exception {
    History ratingHistory = instance.getRatingHistory(RatingType.LADDER_1V1, 123);

    when(restTemplate.getForObject("http://api.example.com/players/123/ratings/1v1/history", History.class))
        .thenReturn(ratingHistory);

    assertThat(ratingHistory.getData().values(), hasSize(3));
    assertThat(ratingHistory.getData().get("1469921413").get(0), is(1026.62f));
    assertThat(ratingHistory.getData().get("1469921413").get(1), is(49.4094f));
  }

  @Test
  public void testUploadMod() throws Exception {
    Path file = Files.createTempFile("foo", null);

    // FIXME filename
    instance.uploadMod(file, (written, total) -> {
    });

    verify(restTemplate).postForObject("http://api.example.com/mods/upload", null, Void.class);
  }

  @Test
  public void testChangePassword() throws Exception {
    instance.changePassword("junit", "currentPasswordHash", "newPasswordHash");

    verify(restTemplate).postForObject("http://api.example.com/users/change_password", null, List.class);
  }

  @Test
  public void testGetCoopMissions() throws Exception {
    instance.getCoopMissions();

    verify(restTemplate).getForObject("http://api.example.com/coop/missions?page%5Bnumber%5D=1", List.class);
    verify(restTemplate).getForObject("http://api.example.com/coop/missions?page%5Bnumber%5D=2", List.class);
  }
}
