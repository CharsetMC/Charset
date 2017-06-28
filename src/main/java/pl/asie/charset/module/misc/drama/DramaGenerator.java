/*
 * Copyright (c) 2015-2016 Adrian Siekierka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.asie.charset.module.misc.drama;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DramaGenerator {
	public static final DramaGenerator INSTANCE = new DramaGenerator();
	private static final Map<String, List<String>> COMBINATIONS = new HashMap<>();
	private static final List<String> SENTENCES = new ArrayList<>();

	static {
		COMBINATIONS.put("people" , Arrays.asList("Player", "jadedcat", "Alblaka", "GregoriousT", "Eloraam", "MrTJP", "mezz",
				"CovertJaguar", "Pahimar", "Sengir", "Azanor", "jeb", "Greymerk", "Dinnerbone", "Grum", "dan200", "Cloudy", "KingLemming",
				"Zeldo", "AlgorithmX2", "Kaelten", "kakermix", "cpw", "LexManos", "LexManos", "Rainwarrior", "Direwolf20", "Calclavia", "Reika", "Reika",
				"Sangar", "skyboy", "amadornes", "AlexisMachina", "shadowfacts", "Simeon", "Kolatra", "BeetoGuy", "Cricket", "BlayTheNinth", "Darkhax",
				"FlowerChild", "SpaceToad", "ChickenBones", "Notch", "Pokefenn", "Shadowclaimer", "Vazkii", "pixlepix", "nekosune", "copygirl", "immibis",
				"mDiyo", "boni", "PowerCrystals", "Soaryn", "Soaryn", "AbrarSyed", "Emoniph", "WayOfTime", "Vexatos", "neptunepink",
				"asie", "tterrag", "Aidan", "Binnie", "Mojang", "ProfMobius", "peterix", "RWTema", "Slowpoke", "Curse", "Curse", "bspkrs", "Mr_okushama",
				"Searge", "iChun", "Krapht", "Erasmus_Crowley", "MysteriousAges", "Drullkus", "Micdoodle8", "GenPage", "Hunterz", "Velotican", "kirindave",
				"MachineMuse", "Lunatrius", "AEnterprise", "AlexIIL", "Speiger", "williewillus"));
		COMBINATIONS.put("sites" , Arrays.asList("FTB Forums", "MCF", "Reddit", "4chan", "Technic Forums", "IC2 Forums", "GitHub", "BitBucket", "IRC",
				"ForgeCraft", "Patreon", "BTW Forums", "GregTech thread", "Google+", "Twitch", "Beam", "Twitter"));
		COMBINATIONS.put("things" , Arrays.asList("ForgeCraft", "Simply Jetpacks", "RedPower 2", "ModLoader", "RedLogic", "Forge MultiPart", "Project: Red",
				"BuildCraft", "Roguelike Dungeons", "IndustrialCraft 2", "Equivalent Exchange", "Project: E", "Forestry", "RailCraft",
				"Compact Solars", "ComputerCraft", "Wireless Redstone", "OpenComputers", "GregTech", "Ars Magica", "Thaumcraft", "FTB", "Technic", "Resonant Rise",
				"MineFactory Reloaded", "Magic Farm 2", "Tekkit", "MCPC+", "ATLauncher", "Metallurgy", "Logistics Pipes", "MCUpdater", "MultiMC", "Curse", "Mojang",
				"Test Pack Please Ignore", "Agrarian Skies", "Sky Factory 2", "InfiTech 2", "FTB Infinity", "Steve's Carts", "BiblioCraft", "Minecraft",
				"XyCraft", "Forge", "GregTech", "Computronics", "Blood Magic", "Botania", "Ars Magica 2", "Thaumcraft", "Factum Opus", "Flamingo", "TIS-3D",
				"Actually Additions", "Zetta Industries",
				"OpenBlocks", "OpenPeripheral", "OpenComputers", "RotaryCraft", "ReactorCraft", "Big Reactors", "Thermal Expansion 4", "Extra Utilities",
				"Universal Electricity", "Just Enough Items", "Highlands", "HelperTools", "Chisels & Bits", "Witchery", "Intangible", "Magneticraft", "Matter Overdrive",
				"Not Enough Items", "Portal Gun", "the Mojang launcher", "Too Many Items", "OptiFine", "Extra Cells", "ExtraBiomesXL", "Biomes O' Plenty",
				"Better Than Wolves", "Tinker's Construct", "Natura", "Hexxit", "Iron Chests", "open-source mods", "closed-source mods", "Not Enough Mods",
				"Ender IO", "Mekanism", "Minecraft 1.7", "Pixelmon", "Pixelmon", "JABBA", "WAILA", "Opis", "CraftGuide", "Iguana Tweaks", "Tinkers Mechworks",
				"the Minecraft Drama Generator", "MineChem", "LittleMaidMob", "MCP", "Immibis' Microblocks", "Carpenter's Blocks", "Chisel", "Applied Energistics",
				"Applied Energistics 2", "Rotatable Blocks", "EnhancedPortals 3", "Ex Nihilo", "Ex Aliquo", "Magic Bees", "BetterStorage", "Backpacks", "Aether II",
				"Highlands", "Alternate Terrain Generation", "Bukkit", "Spigot", "Sponge", "MortTech", "ICBM", "Galacticraft", "Modular Power Suits",
				"Team CoFH", "Extra Bees", "Extra Trees", "Mo' Creatures", "Grimoire of Gaia", "Atum", "Sync", "Hats", "Nether Ores"));
		COMBINATIONS.put("packs" , Arrays.asList("Feed The Beast", "the ForgeCraft pack", "FTB Monster", "FTB Unstable", "Agrarian Skies", "Direwolf20 Pack",
				"Tekkit", "Hexxit", "ATLauncher", "Resonant Rise", "Sky Factory 2", "InfiTech 2", "FTB Infinity", "MCUpdater", "Attack of the B-Team", "Mindcrack", "Magic Maiden", "ForgeCraft", "Technic"));
		COMBINATIONS.put("function" , Arrays.asList("MJ support", "RF support", "EU support", "FMP compatibility", "MCMP compatiblity", "quarries", "automatic mining", "GregTech balance",
				"ComputerCraft APIs", "OpenComputers APIs", "Bukkit plugin compatibility", "MCPC+ support", "ID allocation", "ore processing", "smelting", "crafting", "balance",
				"bees", "ThaumCraft integration", "realism", "decorative blocks", "new mobs", "TCon tool parts", "new wood types", "bundled cable support", "new player capes",
				"more drama", "less drama", "microblocks", "drama generation commands", "Blutricity support", "overpowered items", "underpowered items", "new ores",
				"better SMP support", "achievements", "quests", "more annoying worldgen"));
		COMBINATIONS.put("adj" , Arrays.asList("bad", "wrong", "illegal", "horrible", "nasty", "not on ForgeCraft",
				"noncompliant with Mojang's EULA", "a serious problem", "incompatible", "a waste of time", "wonderful", "amazing", "toxic", "too impl",
				"shameful", "disappointing", "bloated", "outdated", "incorrect", "full of drama", "too realistic"));
		COMBINATIONS.put("badsoft" , Arrays.asList("malware", "spyware", "adware", "DRM", "viruses", "trojans", "keyloggers",
				"stolen code", "easter eggs", "potential login stealers", "adf.ly links", "bad code", "stolen assets", "malicious code", "secret backdoors"));
		COMBINATIONS.put("drama" , Arrays.asList("bugs", "crashes", "drama", "lots of drama", "imbalance", "pain and suffering", "piracy", "bees", "adf.ly"));
		COMBINATIONS.put("crash" , Arrays.asList("crash", "explode", "break", "lag", "blow up", "corrupt chunks", "corrupt worlds", "rain hellfish", "spawn bees"));
		COMBINATIONS.put("ban" , Arrays.asList("ban", "kick", "put a pumpkin of shame on", "add items mocking", "blacklist", "whitelist",
				"give admin rights to", "shame", "destroy"));
		COMBINATIONS.put("code" , Arrays.asList("code", "assets", "ideas", "concepts", "a single function", "5 lines of code", "a class",
				"a few files", "a ZIP file", "Gradle buildscripts", "a GitHub repository"));
		COMBINATIONS.put("worse" , Arrays.asList("worse", "better", "faster", "slower", "more stable", "less buggy"));
		COMBINATIONS.put("ac1" , Arrays.asList("sue", "destroy the life of", "flame", "cause drama about", "complain about", "kick"));
		COMBINATIONS.put("price" , Arrays.asList("150$", "200$", "250$", "300$", "350$", "400$", "450$", "500$", "600$", "650$", "700$"));
		COMBINATIONS.put("activates" , Arrays.asList("activates", "works", "functions", "breaks"));
		COMBINATIONS.put("says" , Arrays.asList("says", "tweets", "claims", "confirms", "denies"));
		COMBINATIONS.put("enormous" , Arrays.asList("big", "large", "huge", "gigantic", "enormous", "surprising"));

		SENTENCES.add("%people% launched a DoS attack on the website of %things%");
		SENTENCES.add("%sites% urges everyone to stop using %things%");
		SENTENCES.add("After a %enormous% amount of requests, %packs% removes %things%");
		SENTENCES.add("After a %enormous% amount of requests, %packs% adds %things%");
		SENTENCES.add("After a %enormous% amount of requests, %packs% adds %function% to %things%");
		SENTENCES.add("%people% plays %things% on Twitch");
		SENTENCES.add("%people% fixes %function% in %things% to be unlike %things%");
		SENTENCES.add("%things% makes %things% %crash%, %sites% users complain");
		SENTENCES.add("%people% complained about being in %things% on %sites%");
		SENTENCES.add("%people% releases %code% of %things% for %price%");
		SENTENCES.add("%sites% considers %things% worse than %things%");
		SENTENCES.add("%people% made %things% depend on %things%");
		SENTENCES.add("%people% bans %people% from using %things% in %packs%");
		SENTENCES.add("%people% complains that %things% discussion doesn't belong on %sites%");
		SENTENCES.add("%people% has a Patreon goal to add %function% to %things% for %price% a month");
		SENTENCES.add("%people% has a Patreon goal to add %things% compatibility to %things% for %price% a month");
		SENTENCES.add("%people% complains that %people% replaced %things% by %things%");
		SENTENCES.add("%people% complains that %people% replaced %things% by %things% in %packs%");
		SENTENCES.add("%people% complains that %people% removed %function% in %packs%");
		SENTENCES.add("%people% decided that %things% is too %adj% and replaced it with %things%");
		SENTENCES.add("%people% %says% %things% is %adj%.");
		SENTENCES.add("%people% %says% %things% is literally %adj%.");
		SENTENCES.add("%things% is not updated for the latest version of Minecraft.");
		SENTENCES.add("%people% removes %things% from %packs%.");
		SENTENCES.add("%people% adds %things% to %packs%.");
		SENTENCES.add("%people% quits modding. Fans of %things% rage.");
		SENTENCES.add("%people% is found to secretly like %things%");
		SENTENCES.add("%people% openly hates %function% in %things%");
		SENTENCES.add("%people% threatens to %ac1% %people% until they remove %things% from %packs%");
		SENTENCES.add("%people% threatens to %ac1% %people% until they remove %function% from %things%");
		SENTENCES.add("%people% threatens to %ac1% %people% until they add %function% to %things%");
		SENTENCES.add("%people% came out in support of %things%");
		SENTENCES.add("%people% came out in support of %drama%");
		SENTENCES.add("%people% and %people% came out in support of %drama%");
		SENTENCES.add("%people% came out against %drama%, %sites% rages");
		SENTENCES.add("%people% and %people% came out against %drama%, %sites% rages");
		SENTENCES.add("%people% forks %things% causing %drama%");
		SENTENCES.add("%people% %says% to replace %things% with %things%");
		SENTENCES.add("%people% %says% %people% causes drama");
		SENTENCES.add("%things% fans claim that %things% should be more like %things%");
		SENTENCES.add("%things% fans claim that %things% should have better %function%");
		SENTENCES.add("%people% %says% that %things% should be more like %things%");
		SENTENCES.add("%people% %says% that %things% should be less like %things%");
		SENTENCES.add("%people% rebalances %things% for %packs%");
		SENTENCES.add("%people% adds %function% to %things% by request of %people%");
		SENTENCES.add("%people% removes %function% from %things% by request of %people%");
		SENTENCES.add("%people% removes compatibility between %things% and %things% by request of %people%");
		SENTENCES.add("%people% %says% %people%'s attitude is %adj%");
		SENTENCES.add("%people% %says% %sites%'s attitude is %adj%");
		SENTENCES.add("%people% quits the development team of %things%");
		SENTENCES.add("%people% %says% %things% is too much like %things%");
		SENTENCES.add("%people% %says% %things% is a ripoff of %things%");
		SENTENCES.add("%people% %says% %people% stole %code% from %people%");
		SENTENCES.add("%people% %says% %people% did not steal %code% from %people%");
		SENTENCES.add("%people% decides to %ban% %people% from %packs%");
		SENTENCES.add("%things% doesn't work with %things% since the latest update");
		SENTENCES.add("%people% sues %things%");
		SENTENCES.add("%people% %says% %things% is %adj% on %sites%");
		SENTENCES.add("%people% %says% %things% is full of %badsoft%");
		SENTENCES.add("%people% %says% %things% causes %drama%");
		SENTENCES.add("%people% %says% %things% causes %drama% when used with %things%");
		SENTENCES.add("%people% %says% using %things% and %things% together is %adj%");
		SENTENCES.add("%people% rants about %things% on %sites%");
		SENTENCES.add("%people% rants about %function% in mods on %sites%");
		SENTENCES.add("%people% steals code from %things%");
		SENTENCES.add("%things% breaks %function%");
		SENTENCES.add("%people% sues %things% developers");
		SENTENCES.add("%people% reminds you that %things% is %adj%");
		SENTENCES.add("%people% and %people% get into a dramatic fight on %sites%");
		SENTENCES.add("Fans of %things% and %things% argue on %sites%");
		SENTENCES.add("%people% and %people% argue about %things%");
		SENTENCES.add("%people% puts %badsoft% in %things%");
		SENTENCES.add("%people% complains about %things% breaking %things%");
		SENTENCES.add("%people% complains about %things% breaking %function%");
		SENTENCES.add("%people% complains about %things% including %function%");
		SENTENCES.add("%things% breaks %function% in %things%");
		SENTENCES.add("%things% breaks %things% support in %things%");
		SENTENCES.add("%things% adds code to %ban% %people% automatically");
		SENTENCES.add("%things% adds code to %ban% people using %things%");
		SENTENCES.add("%things% removes compatibility with %things%");
		SENTENCES.add("%people% %says% not to use %things%");
		SENTENCES.add("%people% %says% not to use %things% with %things%");
		SENTENCES.add("%people% finds %badsoft% in %things%");
		SENTENCES.add("%people% drew a nasty graffiti about %people%");
		SENTENCES.add("%people% drew a nasty graffiti about %things%");
		SENTENCES.add("%things% makes %things% %crash% when used with %things%");
		SENTENCES.add("%things% makes %things% %crash% when used by %people%");
		SENTENCES.add("%things% makes %things% crash %things% when used by %people%");
		SENTENCES.add("%things% adds %badsoft% that only %activates% in %packs%");
		SENTENCES.add("%things% adds %badsoft% that only %activates% alongside %things%");
		SENTENCES.add("%things% makes %people% invincible from %things% in %packs%");
		SENTENCES.add("%people% decides to base their entire modpack on %things%");
		SENTENCES.add("%people% tweaks balance in %things% too much, annoying %sites%");
		SENTENCES.add("%people% tweaks balance in %things% too much, annoying %people%");
		SENTENCES.add("%people% %says% %people% is worse than %people%");
		SENTENCES.add("%people% %says% %things% is %worse% than %things%");
		SENTENCES.add("%people% bans %people% from %sites%");
	}

	private final Random rand = new Random();

	public String generateDrama() {
		return applyReplacements(getRandomDramaTemplate());
	}

	public String generatePersonalizedDrama(String user) {
		return applyReplacements(getRandomDramaTemplate().replaceFirst("%people%", user));
	}

	private String getRandomDramaTemplate() {
		return SENTENCES.get(rand.nextInt(SENTENCES.size()));
	}

	private String applyReplacements(String s) {
		for (String c : COMBINATIONS.keySet()) {
			String cc = "%" + c + "%";
			try {
				for (int i = 0; i < 5; i++) {
					String cm = COMBINATIONS.get(c).get(rand.nextInt(COMBINATIONS.get(c).size()));
					s = s.replaceFirst(cc, cm);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return s;
	}
}
