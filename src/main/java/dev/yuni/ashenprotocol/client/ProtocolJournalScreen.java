package dev.yuni.ashenprotocol.client;

import dev.yuni.ashenprotocol.progress.Progression;
import dev.yuni.ashenprotocol.protocol.ProtocolPhase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ProtocolJournalScreen extends Screen {
    private final Screen parent;
    private int chapter;
    private int page;
    private static final String[] DETAILS = {
        "在工作台让四个红石围绕一颗紫水晶碎片，得到 2 枚协议碎片。碎片既是中继燃料，也是相位手枪弹药。目标：背包中持有协议碎片。",
        "用指南针、上下各一份红石、左右各一枚协议碎片合成熵表。手持右键读数，也可以按 J 打开本面板。世界状态由服务器同步，退出后保存在世界中。",
        "中继配方：第一行铁锭/碎片/铁锭；第二行红石/黑曜石/红石；第三行铁锭/碎片/铁锭。用碎片右键充能，再接红石信号。每枚燃料维持 200 秒，每 5 秒完整度 +1、熵债 -1。只在区块加载时工作。",
        "手枪配方：第一行铜锭/铁锭/碎片；第二行空/铁锭/红石；第三行空/铁锭/空。背包放碎片，右键射击。射程 32 格，伤害 8；消耗一枚碎片并增加 2 点熵债。墙壁阻挡射线。目标：实际开火一次。",
        "死亡会记录最后一次死亡地点。重生后用熵表或本面板查看坐标，回到同一维度的 8 格范围，回收因果残渣。回声不保管普通掉落物，再次死亡会覆盖位置。残渣可无序合成 4 枚碎片。",
        "将末影珍珠放在中间、上下左右各放紫水晶碎片，合成熵晶体。晶体是主动净化的消耗品。目标：背包持有熵晶体。",
        "净化器配方：第一行铁锭/熵表/铁锭；第二行碎片/熵晶体/碎片；第三行铁锭/红石/铁锭。背包放熵晶体，手持右键：消耗晶体，熵债 -250、完整度 +10，冷却 5 秒。没有熵债时不会扣材料。目标：实际净化一次。",
        "建设并维护中继网络，直到完整度至少 3000、熵债低于 1000，且完成一次主动净化。高熵会带来黑暗、残影与视觉闪电。任务只奖励一次经验，死亡不重置。达成重建后仍可继续生存。",
        "灰烬矿石在主世界Y=0～72生成，铁镐以上开采。先准备床、食物、护甲，再收集原灰矿。信号矿在Y=0～40，裂隙矿在Y=-60～0且需钻石镐。新内容只在新生成地形出现。",
        "合成灰烬压制机：铁/碎片/铁，碎片/活塞/碎片，铁/铁/铁。机器接红石，用碎片右键加能量，再用原灰矿右键输入。4秒加工一枚灰烬锭。空手右键收输出，潜行空手取输入；满仓或断电暂停。",
        "定位器配方：空/灰烬锭/空，碎片/指南针/碎片，空/灰烬锭/空。潜行右键切换18类目标，普通右键检索坐标。前四处在主世界，裂隙铸造所在下界，终末档案库在末地。旧世界需向新地形探索。",
        "灰烬锭可制作第一套护甲、灰烬刃与灰烬镐。在原版配方布局中使用灰烬锭；灰烬刃两锭一木棍。进度要求拥有灰烬胸甲。首领战之前建议携带盾牌、食物和备用装备。",
        "四枚灰烬锭围绕协议碎片做余烬钥匙，在灰烬哨站的余烬祭坛右键。余烬守卫160生命，近身火环会提前蓄力；低于半血加快技能。拉开距离避开火环，胜利获得余烬核心。每次召唤消耗一枚钥匙。用信号锭、余烬核心、档案芯片和末影珍珠还能制作坐标门，往返六区域的失序领域；仅在主世界开启，领域内再次使用返回，禁止使用床和重生锚。",
        "信号精炼机配方：灰烬锭/信号晶屑/灰烬锭，碎片/熔炉/碎片，灰烬锭/灰烬锭/灰烬锭。信号晶屑6秒加工成信号锭。每台机器独立存储输入、输出和能量，拆除会掉落材料，但能量不会保留。",
        "潮汐装备以信号锭为主，核心位置替换为余烬核心：头盔与护腿顶部中格、胸甲中心、靴左侧中格。共振卡宾枪用余烬核心、信号锭、碎片制作，背包信号晶屑作为弹药，射程48，伤害14，每枪熵债+4。",
        "四枚信号锭围绕余烬核心做潮汐钥匙，在潮汐档案馆的祭坛召唤织潮者。它有220生命，蓄力后会减速并造成潮汐冲击，掩体可阻断技能视线。掉落潮汐核心，解锁后续装备与区域复苏阵列。",
        "回声织机配方：灰烬锭/线/灰烬锭，碎片/因果回声残渣/碎片，灰烬锭/灰烬锭/灰烬锭。输入因果回声残渣，8秒产出两份编织回声。高阶遗迹也有回声材料。编织回声是复苏电池的关键。",
        "冰原观测站箱子提供霜镜，沙海工坊提供日轮，两处均可用定位器寻找。霜域哨兵也会掉落霜镜；日轮可用记忆碎片、金锭、档案芯片无序合成。档案芯片由石英、信号晶屑和碎片合成。目标同时持有霜镜与日轮。",
        "下界记忆矿石产记忆碎片。灰烬锭+信号锭+记忆碎片无序合成两枚淬炼合金。裂隙熔炉以信号锭、余烬核心、碎片、 blast furnace（高炉）制作；输入裂隙粉尘，10秒产裂隙锭。裂隙护甲核心位置用潮汐核心。",
        "裂隙钥匙：霜镜/淬炼合金/霜镜，淬炼合金/潮汐核心/淬炼合金，霜镜/淬炼合金/霜镜。在下界裂隙铸造所祭坛召唤失谐执政官，300生命，蓄力后绕到身后施加虚弱。获胜掉落裂隙核心。",
        "复苏电池：编织回声+熵晶体+淬炼合金，产2枚。区域复苏阵列：合金/潮汐核心/合金，回声/熵晶体/回声，合金/合金/合金。接红石、碎片充能、输入电池，每10秒完整度+40、熵债-60，可并行维护。",
        "去末地寻找虚空矿石，用钻石级镐采集虚空晶屑。定位器切到终末档案库。坐标钥匙：日轮/裂隙锭/日轮，裂隙锭/裂隙核心/裂隙锭，日轮/裂隙锭/日轮。准备好护甲、弹药和治疗物资，再启动最终祭坛。",
        "最后的测绘者420生命，蓄力后造成黑暗冲击并呼叫裂隙猎手。它不会破坏你的建筑。先清理增援，利用掩体和远程武器，低于半血技能更频繁。胜利掉落坐标核心；可反复挑战但每次都消耗钥匙。",
        "获得坐标核心后，完整度至少8000且熵债低于500，达成复苏新纪元。世界坐标锚需要裂隙锭、裂隙核心、复苏电池和下界之星；输入坐标核心每30秒完整度+500、熵债-1000。也可继续用复苏阵列推进，无需一定使用坐标锚。"
    };
    public static String detail(int id){return id<DETAILS.length?DETAILS[id]:dev.yuni.ashenprotocol.campaign.CampaignJournal.detail(id);}
    public ProtocolJournalScreen(Screen parent){super(Component.literal("协议任务书"));this.parent=parent;}
    private boolean overview=true;
    private Button trackButton,disableButton;
    private int scroll;
    private int panelX,panelY,panelWidth,panelHeight,navWidth,bodyX,bodyY,bodyWidth,bodyBottom;
    private final java.util.List<Button> chapters=new java.util.ArrayList<>();
    public boolean isOverview(){return overview;}
    public int selectedQuest(){return chapter;}
    public void selectQuest(int id){if(id<0||id>=Progression.TITLES.length)return;chapter=id;page=id/8;overview=false;scroll=0;rebuildWidgets();}
    private String status(int id){return ClientState.completed(id)?"✓":dev.yuni.ashenprotocol.progress.QuestCatalog.ready(id,ClientState::completed)?"○":"·";}
    private Button button(String text,int x,int y,int w,int h,Button.OnPress action){Button widget=Button.builder(Component.literal(text),action).bounds(x,y,w,h).build();addRenderableWidget(widget);return widget;}
    @Override protected void init(){
        chapters.clear();panelWidth=Math.min(820,width-24);panelHeight=Math.min(450,height-24);panelX=(width-panelWidth)/2;panelY=(height-panelHeight)/2;
        navWidth=Math.max(96,Math.min(160,panelWidth/4));bodyX=panelX+navWidth+24;bodyY=panelY+65;bodyWidth=panelX+panelWidth-14-bodyX;bodyBottom=panelY+panelHeight-64;
        if(overview){
            int columns=panelWidth>=620?4:3,rows=(12+columns-1)/columns,gap=6,cardWidth=(panelWidth-24-(columns-1)*gap)/columns,cardHeight=Math.min(58,(panelHeight-103-(rows-1)*gap)/rows);
            for(int c=0;c<12;c++){
                final int group=c;int done=0;for(int n=c*8;n<c*8+8;n++)if(ClientState.completed(n))done++;
                button((c+1)+" · "+dev.yuni.ashenprotocol.progress.QuestCatalog.CHAPTERS[c]+" "+done+"/8",panelX+12+c%columns*(cardWidth+gap),panelY+49+c/columns*(cardHeight+gap),cardWidth,cardHeight,b->{int next=group*8;for(int n=group*8;n<group*8+8;n++)if(!ClientState.completed(n)){next=n;break;}selectQuest(next);});
            }
            button("继续推荐任务",panelX+12,panelY+panelHeight-43,112,18,b->{int id=dev.yuni.ashenprotocol.progress.QuestCatalog.recommend(ClientState::completed);selectQuest(id<0?95:id);});
        }else{
            int gap=Math.min(28,(panelHeight-101)/8);
            for(int i=0;i<8;i++){final int id=page*8+i;Button b=Button.builder(Component.literal(status(id)+" "+(id+1)+" · "+Progression.TITLES[id]),q->selectQuest(id)).bounds(panelX+12,panelY+38+i*gap,navWidth,Math.max(14,gap-3)).build();chapters.add(b);addRenderableWidget(b);}
            button("上一章",panelX+12,panelY+panelHeight-63,navWidth/2-2,16,b->selectQuest(Math.floorMod(page-1,12)*8));
            button("下一章",panelX+14+navWidth/2,panelY+panelHeight-63,navWidth/2-2,16,b->selectQuest((page+1)%12*8));
            button("章节总览",panelX+12,panelY+panelHeight-43,navWidth/2-2,16,b->{overview=true;scroll=0;rebuildWidgets();});
            button("推荐",panelX+14+navWidth/2,panelY+panelHeight-43,navWidth/2-2,16,b->{int id=dev.yuni.ashenprotocol.progress.QuestCatalog.recommend(ClientState::completed);if(id>=0)selectQuest(id);});
            trackButton=button("追踪此任务",bodyX,panelY+panelHeight-43,88,18,b->{if(ClientState.connected)dev.yuni.ashenprotocol.network.ProtocolNetwork.track(chapter);});
            disableButton=button("关闭追踪",bodyX+94,panelY+panelHeight-43,72,18,b->{if(ClientState.connected)dev.yuni.ashenprotocol.network.ProtocolNetwork.track(-1);});
        }
        button("返回游戏",width/2-42,panelY+panelHeight-23,84,18,b->onClose());
    }
    private net.minecraft.world.item.ItemStack icon(int id){var item=net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation("ashenprotocol",dev.yuni.ashenprotocol.progress.QuestCatalog.ICONS[id]));return item==null?net.minecraft.world.item.ItemStack.EMPTY:new net.minecraft.world.item.ItemStack(item);}
    private String body(){
        String text="完成条件："+dev.yuni.ashenprotocol.progress.QuestCatalog.condition(chapter)+"\n";
        int target=dev.yuni.ashenprotocol.progress.QuestCatalog.target(chapter);if(target>1)text+="当前行动进度："+ClientState.progress[chapter]+" / "+target+"\n";
        if(chapter==7||chapter==23||chapter==79||chapter==95)text+="世界状态：完整度 "+ClientState.integrity+" / 熵债 "+ClientState.entropy+"\n";
        int[] deps=dev.yuni.ashenprotocol.progress.QuestCatalog.PREREQUISITES[chapter];
        if(deps.length>0){text+="建议先做：";for(int i=0;i<deps.length;i++)text+=(i==0?"":"、")+Progression.TITLES[deps[i]]+(ClientState.completed(deps[i])?"（已完成）":"（待准备）");text+="\n";}
        text+="首次奖励："+dev.yuni.ashenprotocol.progress.QuestCatalog.reward(chapter)+"点经验，完成后自动发放。\n\n操作与材料\n"+detail(chapter);
        int next=dev.yuni.ashenprotocol.progress.QuestCatalog.recommend(ClientState::completed);if(next>=0&&next!=chapter)text+="\n\n下一步推荐："+Progression.TITLES[next]+"。可使用左下方‘推荐’跳转。";
        return text;
    }
    private int maxScroll(){return Math.max(0,font.split(Component.literal(body()),bodyWidth).size()*12-(bodyBottom-bodyY));}
    @Override public boolean mouseScrolled(double mx,double my,double amount){if(!overview&&mx>=bodyX&&mx<bodyX+bodyWidth&&my>=bodyY&&my<bodyBottom){scroll=net.minecraft.util.Mth.clamp(scroll-(int)(amount*24),0,maxScroll());return true;}return super.mouseScrolled(mx,my,amount);}
    @Override public void render(GuiGraphics g,int mx,int my,float delta){
        g.fill(0,0,width,height,0xe009121c);g.fill(panelX,panelY,panelX+panelWidth,panelY+panelHeight,0xff111e2b);g.fill(panelX,panelY,panelX+panelWidth,panelY+2,0xff63e9db);
        int completed=0;for(int i=0;i<96;i++)if(ClientState.completed(i))completed++;
        g.drawString(font,"灰烬协议 / 任务书",panelX+12,panelY+12,0xff63e9db);
        if(overview){
            g.drawString(font,"12章 · "+completed+" / 96 已完成 · 点击章节查看任务",panelX+12,panelY+29,0xffc6d3df);
            int next=dev.yuni.ashenprotocol.progress.QuestCatalog.recommend(ClientState::completed);
            String line=ClientState.connected?(next<0?"全部完成，可继续自由生存。":"建议下一步："+Progression.TITLES[next]):"离线阅读 · 进入世界后同步个人进度";
            g.drawString(font,font.plainSubstrByWidth(line,panelWidth-142),panelX+136,panelY+panelHeight-38,0xffb9a984);
        }else{
            trackButton.active=ClientState.connected&&!ClientState.completed(chapter);disableButton.active=ClientState.connected&&ClientState.tracked>=0;
            g.fill(bodyX-9,panelY+33,bodyX-8,panelY+panelHeight-47,0xff2b3e4d);
            for(int i=0;i<chapters.size();i++){int id=page*8+i;chapters.get(i).active=id!=chapter;chapters.get(i).setMessage(Component.literal(status(id)+" "+(id+1)+" · "+Progression.TITLES[id]));}
            g.renderItem(icon(chapter),bodyX,panelY+32);g.drawString(font,font.plainSubstrByWidth(Progression.TITLES[chapter],bodyWidth-24),bodyX+24,panelY+34,0xfff2eadb);
            boolean done=ClientState.completed(chapter);String state=!ClientState.connected?"离线阅读":done?"已完成 · 首次奖励已自动发放":dev.yuni.ashenprotocol.progress.QuestCatalog.ready(chapter,ClientState::completed)?"可推进 · 可追踪到游戏画面":"待准备 · 可先阅读材料与步骤";
            g.drawString(font,font.plainSubstrByWidth(state,bodyWidth),bodyX,panelY+51,done?0xff63e9db:0xffb9a984);
            scroll=net.minecraft.util.Mth.clamp(scroll,0,maxScroll());g.enableScissor(bodyX,bodyY,bodyX+bodyWidth,bodyBottom);int y=bodyY-scroll;
            for(var line:font.split(Component.literal(body()),bodyWidth)){g.drawString(font,line,bodyX,y,0xffc6d3df);y+=12;}g.disableScissor();
            if(maxScroll()>0){int track=bodyBottom-bodyY,thumb=Math.max(8,track*track/(track+maxScroll())),top=bodyY+scroll*(track-thumb)/maxScroll();g.fill(bodyX+bodyWidth+3,bodyY,bodyX+bodyWidth+5,bodyBottom,0xff293944);g.fill(bodyX+bodyWidth+3,top,bodyX+bodyWidth+5,top+thumb,0xff63e9db);}
        }
        super.render(g,mx,my,delta);
        if(!overview&&mx>=bodyX&&mx<bodyX+16&&my>=panelY+32&&my<panelY+48)g.renderTooltip(font,icon(chapter),mx,my);
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
}
