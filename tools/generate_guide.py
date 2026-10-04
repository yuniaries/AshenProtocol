"""Produce the complete survival guide from the current journal and recipe data."""
from pathlib import Path
import json,re
root=Path(__file__).resolve().parents[1];r=root/'src/main/resources';lang=json.loads((r/'assets/ashenprotocol/lang/zh_cn.json').read_text());version=dict(x.split('=',1) for x in (root/'gradle.properties').read_text().splitlines() if '=' in x)['mod_version']
vanilla={'iron_ingot':'铁锭','copper_ingot':'铜锭','redstone':'红石粉','amethyst_shard':'紫水晶碎片','ender_pearl':'末影珍珠','compass':'指南针','obsidian':'黑曜石','book':'书','string':'线','piston':'活塞','furnace':'熔炉','blast_furnace':'高炉','stick':'木棍','nether_star':'下界之星','blue_ice':'蓝冰','gold_ingot':'金锭','quartz':'下界石英','coal':'煤炭','bone_meal':'骨粉','wheat_seeds':'小麦种子','wheat':'小麦','sweet_berries':'甜浆果','kelp':'海带','potato':'马铃薯','carrot':'胡萝卜','cocoa_beans':'可可豆','melon_slice':'西瓜片','dandelion':'蒲公英','glass_bottle':'玻璃瓶','bowl':'碗','brick':'红砖','paper':'纸','iron_nugget':'铁粒','bucket':'桶','milk_bucket':'牛奶桶','composter':'堆肥桶','brewing_stand':'酿造台','smoker':'烟熏炉','loom':'织布机','comparator':'红石比较器','glass':'玻璃'}
def label(id):
 if ':' not in id:id='ashenprotocol:'+id
 ns,name=id.split(':');return vanilla.get(name,name) if ns=='minecraft' else lang.get('item.'+ns+'.'+name,lang.get('block.'+ns+'.'+name,name))
manual=f'''# 灰烬协议：生态重启 {version} · 完整游玩指南

作者：yuniaries · Minecraft 1.20.1 / Forge 47.3.22 / Java 17（64位）
[作者主页](https://github.com/yuniaries) · [项目仓库](https://github.com/yuniaries/AshenProtocol)

## 安装与教程

解压后直接运行根目录 `Plain Craft Launcher 2.exe`，把下载的完整 ZIP 拖入 PCL 安装。已有 PCL 可直接拖入 ZIP。首次安装需要联网下载 Minecraft、Forge 和运行库；原创 MOD 已经内置。选择新安装的“生态重启 {version}”实例。建议分配4GB内存。Java需64位17或兼容运行时，启动器会检测。无需运行启动BAT。

建议新建生存世界，难度普通；简单和困难也支持。新增领域可能使 Minecraft 提示“实验性设置”，在自己的新世界确认后点继续。旧存档先备份，新矿石和遗迹只出现在未生成区域。包中不附带或替换你的存档。

进入世界按 **J** 打开协议终端，共 **96项目标**，左侧“上一组 / 下一组”翻页，正文用滚轮阅读。P是原版社交键。首次进入领取野外手册，手持右键也能打开教程；丢失后用书与红石粉合成。GUI缩放建议自动，支持全屏。

## 游玩路线与准备

路线为：建立基地与协议网络 → 灰烬矿和初阶装备 → 余烬、潮汐守卫 → 两极档案与下界裂隙 → 末地坐标核心 → 农业、钢铁和自动化 → 深层领域四大据点 → 谐振装备与生态复苏 → 六波远征和完整复苏。

原版下界、末地仍参与流程，准备铁镐、钻石镐、盾牌、食物与传送门材料。每次召唤消耗钥匙，核心会被装备、钥匙、机器配方消耗，需要反复挑战。不要以为背包只留一个核心就能合成所有后续装备。

和平难度可建造、农业和生产，但无法自然遭遇怪物或召唤首领。挑战请切换到简单、普通或困难。首领不会破坏建筑；主动挑战前清空周边空间，避免与家畜和生产基地挤在一起。

## 两个原创领域与18处遗迹

坐标门在主世界使用可进入失序领域，内部有余灰荒原、信号林地、静潮湿地、镜霜原野、日轮裂土、失谐高地。再次使用返回原主世界坐标。深层坐标门在主世界进入回声深层，包含深渊黑岸、风暴台地、根源密林、曙光废土、记忆沼地、谐振山脊，内部再次使用返回。两种门必须从主世界出发。

**领域中不要用床或重生锚。** 深层领域没有自然天光，农田需要灯光。返回需要仍持有对应坐标门；死亡掉落后可重新制作。出生传送会寻找安全地面，必要时在空地创建落脚平台；不会凭空提供整套基地。

遗迹定位器潜行右键切换18个目标，普通右键检索坐标，冷却10秒。目标在不同维度，先去正确维度再定位。未找到时换区域，旧地图探索未生成地形。

| 地区 | 遗迹 |
| --- | --- |
| 主世界 / 失序领域 | 灰烬哨站、潮汐档案馆、冰原观测站、沙海工坊、培育者补给站、信号交易所、风暴观测所、根系养护站 |
| 下界 | 裂隙铸造所、下界泵站 |
| 末地 | 终末档案库、终界装配厂 |
| 回声深层 | 深渊大教堂、风暴堡垒、根源藏库、曙光高塔、记忆实验室、回声采掘站 |

补给站提供农田和种子，交易所提供契约终端，实验室与装配厂含加工设备，采掘站提供矿产和材料。箱子还可能带来有用的后期组件。四种新普通敌人分别掉落深矿碎片、风暴晶屑、根系树脂、曙光碎片，支持重复获取。

## 农业、补给与自动化

八种作物的种子由小麦种子、协议碎片和对应原版作物合成，也可在遗迹发现或通过契约终端交换。在湿润耕地种植，光照至少9，支持骨粉。成熟后得到作物和种子；提前挖掉只返回种子。

24种食品提供饥饿值和饱和度；霜寒口粮提供抗性，耐热口粮提供抗火，回声口粮提供夜视，治疗肉汤提供再生，守卫盛宴提供伤害吸收，曙光盛宴提供强化再生。补给胶囊和绷带手持右键使用；满血不会扣绷带，无中毒、虚弱或减速时不会扣净毒胶囊。物品冷却由服务端检查。

机器均需红石信号和协议碎片充能，每片200能量，上限4000；运行每秒消耗1能量。主材料从顶部漏斗进，辅助材料和碎片从侧面漏斗进，成品从底部漏斗出。手动右键放材料，空手收成品，潜行空手取回两种输入。双材料不足、断电或输出满时暂停。只在区块加载时运行；拆除掉落库存，能量不会作为物品返还。

基础顺序：灰烬压制机 → 碳化窑与拉丝机 → 炼钢炉 → 电路装配机 → 精密制造。炼钢炉以灰烬锭制造，避免要求尚未生产的钢锭。

| 机器 | 主输入1份 | 辅助输入 | 产物 / 世界变化 | 耗时 |
| --- | --- | --- | --- | --- |
'''
processing=json.loads((root/'docs/PROCESSING.json').read_text())
for machine,v in processing.items():
 result=label(v['output'])+' ×'+str(v['count']) if v['output'] else {'restoration_array':'完整度+40，熵债-60','world_anchor':'完整度+500，熵债-1000','biosphere_restorer':'完整度+200，熵债-300'}[machine]
 manual+='| '+label(machine)+' | '+label(v['input'])+' | '+(str(v['secondaryCount'])+' × '+label(v['secondary']) if v['secondary'] else '无')+' | '+result+' | '+str(v['ticks']//20)+'秒 |\n'
manual+='''
## 委托、远征与世界复苏

契约终端潜行右键选委托，手持要求的材料提交，奖励3～10枚凭证与经验，每人整理冷却60秒。手持2凭证可换4份当前对应作物的种子。委托材料来自采矿、农田和生产。奖励不会在重复点击时无限发放。

远征信标需要手持远征封印启动；六波敌人逐渐增加，最后是余烬守卫。必须保持存活、在同一维度的48格范围内；离开、死亡、掉线或主人潜行空手取消会结束挑战。取消不返封印。通关获得16凭证、3精密齿轮，返还封印并完成任务。敌人有自己的归属标签，不会误清理野生敌人或其他信标的挑战。

完整度和熵债由全世界共享，任务与死亡回声按玩家保存。旧24项进度自动迁移，死亡不重置，首次奖励只领取一次。通过中继、净化器、复苏阵列与生命圈复苏器改善世界；武器使用会增加熵债，不能只射击而不维护网络。数值范围0～10000。

最终目标为：亲自击败曙光执政官、完成六波远征、完整度≥9500且熵债<200。该目标可与其他玩家共同建设世界，但首领与远征记录属于参与者本人。完成后可继续生存和挑战。

## 96项完整目标与操作教程

'''
journal=(root/'src/main/java/dev/yuni/ashenprotocol/client/ProtocolJournalScreen.java').read_text();part=journal.split('private static final String[] DETAILS = {',1)[1].split('\n    };',1)[0];details=re.findall(r'"((?:[^"\\]|\\.)*)"',part)
progress=(root/'src/main/java/dev/yuni/ashenprotocol/progress/Progression.java').read_text();titles=re.findall(r'"([^"]+)"',progress.split('String[] TITLES = {',1)[1].split('};',1)[0]);assert len(details)==24 and len(titles)==96
for i,text in enumerate(details):manual+='### '+str(i+1)+' · '+titles[i]+'\n\n'+text.replace('切换六类目标','切换18类目标')+'\n\n'
manual+=(root/'docs/CAMPAIGN_GUIDE.md').read_text().split('\n\n',1)[1].replace('\n## ','\n### ')
manual+='\n## 全部配方\n\n工作台配方“空”代表空槽；无序配方材料可任意摆放。熔炉配方单独标注。机器加工见前表。\n\n'
for path in sorted((r/'data/ashenprotocol/recipes').glob('*.json')):
 data=json.loads(path.read_text());result=data['result'];id=result if isinstance(result,str) else result['item'];count=1 if isinstance(result,str) else result.get('count',1);manual+=f'### {label(id)} ×{count}\n\n'
 if data['type']=='minecraft:smelting':manual+='熔炉：'+label(data['ingredient']['item'])+' → '+label(id)+f'，{data["cookingtime"]//20}秒。\n\n'
 elif 'pattern' in data:
  rows=[row.ljust(3) for row in data['pattern']]+['   ']*(3-len(data['pattern']));manual+='| 左 | 中 | 右 |\n| --- | --- | --- |\n'
  for row in rows:manual+='| '+' | '.join('空' if c==' ' else label(data['key'][c]['item']) for c in row)+' |\n'
  manual+='\n'
 else:manual+=' + '.join(label(i['item']) for i in data['ingredients'])+'（无序合成）。\n\n'
manual+='## 故障排查\n\n按J无响应时查看按键冲突和MOD列表；机器暂停时检查红石、能量、两种输入、满仓和区块加载；祭坛检查钥匙、难度、已有首领与空间；旧地图找不到内容需探索新地形。启动页两条链接均指向yuniaries本人。包只包含原创核心和PCL，基础运行库由PCL联网安装；压缩体积不能代表游戏时长或与数百个成熟MOD的等价关系。\n'
(root/'docs/PLAY_GUIDE.md').write_text('\n'.join(x.rstrip() for x in manual.splitlines()).rstrip()+'\n');print(root/'docs/PLAY_GUIDE.md')
