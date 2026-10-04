from pathlib import Path
import re,json
root=Path(__file__).resolve().parents[1];base=root/'src/main/java/dev/yuni/ashenprotocol';s=(base/'progress/Progression.java').read_text()
titles=re.findall(r'"([^"]+)"',s.split('String[] TITLES = {')[1].split('};')[0]);goals=json.loads((root/'docs/campaign-goals.json').read_text());items=['protocol_fragment','entropy_meter','protocol_relay','phase_pistol','echo_residue','entropy_crystal','entropy_sink','protocol_relay','raw_ash','ash_ingot','archive_compass','ash_chestplate','cinder_core','signal_ingot','tide_chestplate','tide_core','woven_echo','sun_disk','rift_ingot','rift_core','restoration_array','void_shard','atlas_core','world_anchor']+[r['item'] or ('biosphere_core' if r['index']==77 else 'expedition_beacon' if r['index']==78 else 'field_guide') for r in goals]
# These are guidance prerequisites, not new completion gates. Branches remain playable independently.
deps={0:[],1:[0],2:[0],3:[0],4:[],5:[],6:[1,5],7:[2,6],8:[0],9:[8],10:[9],11:[9],12:[10,11],13:[12],14:[13],15:[12,14],16:[9],17:[10],18:[13],19:[15,17,18],20:[16,19],21:[19],22:[21],23:[20,22]}
for n in range(24,32):deps[n]=[0]
deps.update({32:[24,26,28],33:[31],34:[41],35:[9],36:[9],37:[35,36],38:[37,13],39:[24],40:[39,29],41:[25,31],42:[16,29],43:[37],44:[38],45:[37,36],46:[37,36],47:[45],48:[13],49:[13,5],50:[38],51:[50],52:[19,38],53:[18,42],54:[22,43,53],55:[49,54],56:[55],57:[56],58:[49],59:[57,49,22],60:[57,62],61:[59],62:[61],63:[55],64:[53,62,63],65:[62,63,53],66:[65],67:[66],68:[55],69:[67,68,41],70:[68,40],71:[69],72:[71],73:[55],74:[72,73,54],75:[74],76:[75],77:[62,67,72,76,54],78:[52,51],79:[75,78],80:[9],81:[9],82:[24],83:[51],84:[33],85:[57,58],86:[77],87:[22],88:[55],89:[12],90:[55],91:[78],92:[75],93:[80],94:[24,25,26,27,28,29,30,31],95:[79]})
counters={80:('AshenJobs',100),81:('AshenAutoOutputs',64),82:('AshenHarvests',32),83:('AshenContracts',10),84:('AshenMedicalUses',10),85:('AshenPhaseJobs',10),86:('AshenBiosphereJobs',1),87:('AshenAnchorJobs',1),91:('AshenArenaWins',3),93:('AshenJobs',1000)}
chapters=['接入与重建','余灰与潮汐','裂隙与终末','农田与种源','补给与钢铁','生物与精密','档案与深层门','深渊探索','风暴与根源','曙光与复苏','生产精通','远征与全境']
# Recommend the playable backbone first; recovering a death echo remains an optional branch.
order=[0,1,8,9,10,11,12,13,14,15,16,17,18,19,21,22,24,25,26,27,28,29,30,31,35,36,37,38,39,40,41,42,43,45,46,47,48,49,53,54,55,56,57,58,59,61,62,63,65,66,67,68,69,71,72,73,74,75,76,77,50,51,52,78,2,3,5,6,7,20,23,79]+[i for i in range(96) if i not in [0,1,8,9,10,11,12,13,14,15,16,17,18,19,21,22,24,25,26,27,28,29,30,31,35,36,37,38,39,40,41,42,43,45,46,47,48,49,53,54,55,56,57,58,59,61,62,63,65,66,67,68,69,71,72,73,74,75,76,77,50,51,52,78,2,3,5,6,7,20,23,79]]
source='''package dev.yuni.ashenprotocol.progress;
import java.util.function.IntPredicate;
import net.minecraft.server.level.ServerPlayer;
public final class QuestCatalog {
'''
def strings(xs):return '{'+','.join(json.dumps(x,ensure_ascii=False) for x in xs)+'}'
source+=' public static final String[] CHAPTERS='+strings(chapters)+';\n public static final String[] ICONS='+strings(items)+';\n public static final int[][] PREREQUISITES={'+','.join('{'+','.join(map(str,deps[i]))+'}' for i in range(96))+'};\n'
source+=' private static final int[] ORDER={'+','.join(map(str,order))+'};\n'
source+=''' public static boolean ready(int id,IntPredicate done){for(int p:PREREQUISITES[id])if(!done.test(p))return false;return true;}
 public static int recommend(IntPredicate done){for(int id:ORDER)if(!done.test(id)&&ready(id,done))return id;return -1;}
 public static int target(int id){return switch(id){'''+''.join('case '+str(i)+' -> '+str(v[1])+';' for i,v in counters.items())+'''case 89,90 -> 6;case 92,94 -> 8;default -> 1;};}
 public static int progress(ServerPlayer p,int id){return switch(id){'''+''.join('case '+str(i)+' -> Progression.counter(p,"'+v[0]+'");' for i,v in counters.items())+'''case 89 -> Integer.bitCount(Progression.counter(p,"AshenLostVisits"));case 90 -> Integer.bitCount(Progression.counter(p,"AshenDeepVisits"));case 92 -> Integer.bitCount(Progression.counter(p,"AshenBossKills"));case 94 -> Integer.bitCount(Progression.counter(p,"AshenHarvestKinds"));default -> Progression.completed(p,id)?1:0;};}
 public static String condition(int id){return switch(id){case 3 -> "实际成功开火一次";case 4 -> "背包持有因果回声残渣";case 6 -> "实际使用一次净化器";case 7 -> "主动净化已完成，完整度≥3000、熵债<1000";case 17 -> "同时持有霜镜与日轮";case 23 -> "坐标核心记录完成，完整度≥8000、熵债<500";case 51 -> "成功提交一次委托";case 61,66,71,75 -> "亲自击败对应首领";case 78 -> "完成一次六波远征";case 79 -> "曙光战与远征完成，完整度≥9500、熵债<200";case 89,90 -> "实际走访全部六类原创群系";case 92 -> "亲自击败八种首领各一次";case 94 -> "亲自收获八种成熟原创作物";case 95 -> "完整复苏已完成，完整度10000、熵债0";default -> id>=80 ? "累计真实行动达到目标值" : "背包持有目标物品；不额外扣除";};}
 public static int reward(int id){return 15+Math.min(id,30)*5;}
 public static int[] progressValues(ServerPlayer p){int[] v=new int[96];for(int i=0;i<v.length;i++)v[i]=Math.min(target(i),progress(p,i));return v;}
}
'''
source=source.replace(' public static int reward',' public static String hint(int id){return switch(id){case 0->"紫水晶 + 四份红石 → 协议碎片";case 1->"指南针、红石与碎片 → 熵表";case 8->"铁镐开采 Y=0～72 的灰烬矿";case 9->"压制原灰矿，获取灰烬锭";case 12->"哨站祭坛使用余烬钥匙";case 51->"契约终端手持足量材料提交";case 78->"信标使用封印，清除六波敌人";default->"按 J 查看配方、材料与操作步骤";};}\n'+' public static int reward')
(base/'progress/QuestCatalog.java').write_text(source)
(root/'docs/QUEST_BOOK.json').write_text(json.dumps([{'id':i,'title':titles[i],'chapter':chapters[i//8],'icon':items[i],'prerequisites':deps[i]} for i in range(96)],ensure_ascii=False,indent=2)+'\n')
