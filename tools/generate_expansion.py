"""Original data, block/item/armor/entity pixels and six authored structure templates.
No files are imported from another modpack. Standard Minecraft model parents/AI are used.
"""
from pathlib import Path
import json,struct,zlib,gzip,hashlib
R=Path(__file__).resolve().parents[1]/'src/main/resources'
def save(path,data):
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def ap(s):return 'ashenprotocol:'+s
def item(s):return {'item':s if ':' in s else ap(s)}
def shaped(name,pattern,key,result=None,count=1):
 save(f'data/ashenprotocol/recipes/{name}.json',{'type':'minecraft:crafting_shaped','pattern':pattern,'key':{k:item(v) for k,v in key.items()},'result':{'item':ap(result or name),'count':count}})
def shapeless(name,ingredients,result=None,count=1):
 save(f'data/ashenprotocol/recipes/{name}.json',{'type':'minecraft:crafting_shapeless','ingredients':[item(s) for s in ingredients],'result':{'item':ap(result or name),'count':count}})
zh={'raw_ash':'原灰矿','signal_shard':'信号晶屑','rift_dust':'裂隙粉尘','memory_shard':'记忆碎片','void_shard':'虚空晶屑','ash_ingot':'灰烬锭','signal_ingot':'信号锭','rift_ingot':'裂隙锭','woven_echo':'编织回声','tempered_alloy':'淬炼合金','restoration_cell':'复苏电池','cinder_core':'余烬核心','tide_core':'潮汐核心','rift_core':'裂隙核心','atlas_core':'坐标核心','frost_lens':'霜镜','sun_disk':'日轮','archive_chip':'档案芯片','cinder_key':'余烬钥匙','tide_key':'潮汐钥匙','rift_key':'裂隙钥匙','atlas_key':'坐标钥匙','archive_compass':'遗迹定位器','resonance_carbine':'共振卡宾枪','rift_lance':'裂隙光矛','atlas_caster':'坐标投射器','ash_ore':'灰烬矿石','signal_ore':'信号矿石','rift_ore':'裂隙矿石','memory_ore':'下界记忆矿石','void_ore':'末地虚空矿石','ash_press':'灰烬压制机','crystal_refinery':'信号精炼机','echo_loom':'回声织机','alloy_forge':'裂隙熔炉','restoration_array':'区域复苏阵列','world_anchor':'世界坐标锚','cinder_shrine':'余烬祭坛','tide_shrine':'潮汐祭坛','rift_shrine':'裂隙祭坛','atlas_shrine':'坐标祭坛','archive_bricks':'档案砖','signal_glass':'信号晶板','ash_tiles':'灰烬铺砖','rift_tiles':'裂隙铺砖'}
enemies={'ash_stalker':'灰烬潜行者','marsh_lurker':'沼泽伏影','frost_sentinel':'霜域哨兵','rift_hound':'裂隙猎手','cinder_warden':'余烬守卫','tide_weaver':'织潮者','discord_archon':'失谐执政官','last_cartographer':'最后的测绘者'}
blocks={k for k in zh if k.endswith(('_ore','_shrine','_tiles'))}|{'ash_press','crystal_refinery','echo_loom','alloy_forge','restoration_array','world_anchor','archive_bricks','signal_glass'}
for prefix,name in [('ash','灰烬'),('tide','潮汐'),('rift','裂隙')]:
 for suffix,label in [('blade','刃'),('pickaxe','镐'),('helmet','头盔'),('chestplate','胸甲'),('leggings','护腿'),('boots','靴')]:zh[prefix+'_'+suffix]=name+label
for id,name in enemies.items():zh[id+'_spawn_egg']=name+'刷怪蛋'

def texture(path,w,h,fn):
 data=b''.join(b'\0'+bytes(sum((list(fn(x,y)) for x in range(w)),[])) for y in range(h))
 def c(n,d):return struct.pack('>I',len(d))+n+d+struct.pack('>I',zlib.crc32(n+d)&0xffffffff)
 p=R/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b'\x89PNG\r\n\x1a\n'+c(b'IHDR',struct.pack('>IIBBBBB',w,h,8,6,0,0,0))+c(b'IDAT',zlib.compress(data))+c(b'IEND',b''))
palettes=[(230,116,66),(65,209,201),(151,125,233),(116,168,212),(218,184,96),(98,223,161)]
for n,(id,name) in enumerate(zh.items()):
 col=palettes[n%len(palettes)];seed=int(hashlib.sha256(id.encode()).hexdigest()[:8],16)
 if id in blocks:
  def pixel(x,y,col=col,seed=seed,id=id):
   shade=38+((x*17+y*31+seed)%23);base=(shade,shade+5,shade+12)
   if id.endswith('_ore'):
    if ((x*73+y*37+seed)%29)<6:base=col
   elif x in (1,14) or y in (1,14):base=(103,127,142)
   elif (x in (4,11) and 4<=y<=11) or (y in (4,11) and 4<=x<=11) or (x+y+seed)%13==0:base=col
   return (*base,255)
  texture(f'assets/ashenprotocol/textures/block/{id}.png',16,16,pixel)
  save(f'assets/ashenprotocol/blockstates/{id}.json',{'variants':{'':{'model':ap('block/'+id)}}})
  save(f'assets/ashenprotocol/models/block/{id}.json',{'parent':'minecraft:block/cube_all','textures':{'all':ap('block/'+id)}})
  save(f'assets/ashenprotocol/models/item/{id}.json',{'parent':ap('block/'+id)})
  save(f'data/ashenprotocol/loot_tables/blocks/{id}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':ap(id)}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 else:
  def pixel(x,y,col=col,id=id,seed=seed):
   shape=2<x<13 and 3<y<13
   if id.endswith(('_blade','_pickaxe')) or id in ('resonance_carbine','rift_lance','atlas_caster'):shape=abs(x+y-15)<3 and 2<x<14 and 2<y<14
   if id.endswith('helmet'):shape=2<x<13 and 3<y<10 and not(5<x<10 and y>6)
   if id.endswith('chestplate'):shape=3<y<14 and (4<x<11 or y<7 and 1<x<14)
   if id.endswith('leggings'):shape=3<y<14 and 3<x<12 and not(6<x<9 and y>7)
   if id.endswith('boots'):shape=7<y<14 and (2<x<7 or 8<x<13)
   if not shape:return (0,0,0,0)
   return (*(col if (x+y+seed)%5 else (216,230,235)),255)
  texture(f'assets/ashenprotocol/textures/item/{id}.png',16,16,pixel)
  if id.endswith('spawn_egg'):save(f'assets/ashenprotocol/models/item/{id}.json',{'parent':'minecraft:item/template_spawn_egg'})
  else:save(f'assets/ashenprotocol/models/item/{id}.json',{'parent':'minecraft:item/handheld' if id.endswith(('_blade','_pickaxe')) else 'minecraft:item/generated','textures':{'layer0':ap('item/'+id)}})
for index,(id,name) in enumerate(enemies.items()):
 col=palettes[index%6]
 def skin(x,y,col=col,index=index):
  v=(28,42,54)
  if x%8 in (0,7) or y%8 in (0,7):v=(75,89,104)
  if (x+y*(index+1))%11<2:v=col
  if 9<=x<=14 and y==12:v=col # bright eyes in head UV
  return (*v,255)
 texture(f'assets/ashenprotocol/textures/entity/{id}.png',64,64,skin)
 drop=['raw_ash','signal_shard','frost_lens','rift_dust','cinder_core','tide_core','rift_core','atlas_core'][index]
 save(f'data/ashenprotocol/loot_tables/entities/{id}.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':ap(drop),'functions':[{'function':'minecraft:set_count','count':1 if index>=4 else {'type':'minecraft:uniform','min':1,'max':2}}]}]}]})
for idx,id in enumerate(['ash','tide','rift']):
 for layer in [1,2]:
  col=palettes[idx]
  texture(f'assets/ashenprotocol/textures/models/armor/{id}_layer_{layer}.png',64,32,lambda x,y,col=col: (*((col if (x+y)%11<2 else (52,68,83)) if x%8 not in (0,7) else (151,168,182)),255))
for lang in ['zh_cn','en_us']:
 p=f'assets/ashenprotocol/lang/{lang}.json';d=json.loads((R/p).read_text())
 for id,name in zh.items():d[('block.' if id in blocks else 'item.')+'ashenprotocol.'+id]=name if lang=='zh_cn' else id.replace('_',' ').title()
 for id,name in enemies.items():d['entity.ashenprotocol.'+id]=name if lang=='zh_cn' else id.replace('_',' ').title()
 save(p,d)
# Real processing recipes are implemented server-side; ordinary crafting provides a readable survival route.
shaped('ash_press',['IFI','FCF','III'],{'I':'minecraft:iron_ingot','F':'protocol_fragment','C':'minecraft:piston'})
shaped('crystal_refinery',['ASA','FCF','AAA'],{'A':'ash_ingot','S':'signal_shard','F':'protocol_fragment','C':'minecraft:furnace'})
shaped('echo_loom',['ASA','FCF','AAA'],{'A':'ash_ingot','S':'minecraft:string','F':'protocol_fragment','C':'echo_residue'})
shaped('alloy_forge',['ASA','FCF','AAA'],{'A':'signal_ingot','S':'cinder_core','F':'protocol_fragment','C':'minecraft:blast_furnace'})
shaped('restoration_array',['ASA','FCF','AAA'],{'A':'tempered_alloy','S':'tide_core','F':'woven_echo','C':'entropy_crystal'})
shaped('world_anchor',['ASA','FCF','AAA'],{'A':'rift_ingot','S':'rift_core','F':'restoration_cell','C':'minecraft:nether_star'})
shapeless('tempered_alloy',['ash_ingot','signal_ingot','memory_shard'],count=2)
shapeless('restoration_cell',['woven_echo','entropy_crystal','tempered_alloy'],count=2)
shaped('archive_compass',[' A ','FCF',' A '],{'A':'ash_ingot','F':'protocol_fragment','C':'minecraft:compass'})
shaped('cinder_key',[' A ','ACA',' A '],{'A':'ash_ingot','C':'protocol_fragment'})
shaped('tide_key',[' A ','ACA',' A '],{'A':'signal_ingot','C':'cinder_core'})
shaped('rift_key',['LAL','ACA','LAL'],{'A':'tempered_alloy','C':'tide_core','L':'frost_lens'})
shaped('atlas_key',['LAL','ACA','LAL'],{'A':'rift_ingot','C':'rift_core','L':'sun_disk'})
for id,mat,core in [('resonance_carbine','signal_ingot','cinder_core'),('rift_lance','rift_ingot','tide_core'),('atlas_caster','rift_ingot','atlas_core')]:
 shaped(id,['CIF',' II',' I '],{'C':core,'I':mat,'F':'woven_echo' if id=='atlas_caster' else 'protocol_fragment'})
for prefix,mat,gate in [('ash','ash_ingot','ash_ingot'),('tide','signal_ingot','cinder_core'),('rift','rift_ingot','tide_core')]:
 shaped(prefix+'_blade',[' G ',' I ',' S '],{'G':gate,'I':mat,'S':'minecraft:stick'})
 shaped(prefix+'_pickaxe',['IGI',' S ',' S '],{'G':gate,'I':mat,'S':'minecraft:stick'})
 for suffix,pattern in [('helmet',['IGI','I I']),('chestplate',['I I','IGI','III']),('leggings',['IGI','I I','I I']),('boots',['I I','G I'])]:shaped(prefix+'_'+suffix,pattern,{'I':mat,'G':gate})
for id,mat in [('archive_bricks','ash_ingot'),('signal_glass','signal_shard'),('ash_tiles','raw_ash'),('rift_tiles','rift_dust')]:shaped(id,['MM','MM'],{'M':mat},count=8)
# Renewable late-stage crafting uses actual boss/region drops, not an invisible timed reward.
shapeless('frost_lens_from_shards',['signal_shard','minecraft:blue_ice','archive_chip'],'frost_lens',2)
shapeless('sun_disk_from_memory',['memory_shard','minecraft:gold_ingot','archive_chip'],'sun_disk',2)
shapeless('archive_chip_from_quartz',['minecraft:quartz','signal_shard','protocol_fragment'],'archive_chip',2)
# Ore drops preserve silk touch, fortune and tool requirements.
for ore,drop in zip(['ash_ore','signal_ore','rift_ore','memory_ore','void_ore'],['raw_ash','signal_shard','rift_dust','memory_shard','void_shard']):
 save(f'data/ashenprotocol/loot_tables/blocks/{ore}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:alternatives','children':[{'type':'minecraft:item','name':ap(ore),'conditions':[{'condition':'minecraft:match_tool','predicate':{'enchantments':[{'enchantment':'minecraft:silk_touch','levels':{'min':1}}]}}]},{'type':'minecraft:item','name':ap(drop),'functions':[{'function':'minecraft:apply_bonus','enchantment':'minecraft:fortune','formula':'minecraft:ore_drops'},{'function':'minecraft:explosion_decay'}]}]}]}]})
save('data/minecraft/tags/blocks/mineable/pickaxe.json',{'replace':False,'values':[ap(s) for s in blocks]+[ap('protocol_relay')]})
save('data/minecraft/tags/blocks/needs_iron_tool.json',{'replace':False,'values':[ap(s) for s in ['ash_ore','signal_ore','memory_ore']]})
save('data/minecraft/tags/blocks/needs_diamond_tool.json',{'replace':False,'values':[ap(s) for s in ['rift_ore','void_ore']]})
for ore,dim,target,size,low,high,count in [('ash_ore','overworld','minecraft:stone_ore_replaceables',7,0,72,10),('signal_ore','overworld','minecraft:stone_ore_replaceables',5,0,40,7),('rift_ore','overworld','minecraft:deepslate_ore_replaceables',4,-60,0,4),('memory_ore','nether','minecraft:base_stone_nether',6,8,112,8),('void_ore','end','minecraft:end_stone',4,0,72,5)]:
 rule={'predicate_type':'minecraft:block_match','block':target} if dim=='end' else {'predicate_type':'minecraft:tag_match','tag':target}
 save(f'data/ashenprotocol/worldgen/configured_feature/{ore}.json',{'type':'minecraft:ore','config':{'size':size,'discard_chance_on_air_exposure':0,'targets':[{'target':rule,'state':{'Name':ap(ore)}}]}})
 save(f'data/ashenprotocol/worldgen/placed_feature/{ore}.json',{'feature':ap(ore),'placement':[{'type':'minecraft:count','count':count},{'type':'minecraft:in_square'},{'type':'minecraft:height_range','height':{'type':'minecraft:uniform','min_inclusive':{'absolute':low},'max_inclusive':{'absolute':high}}},{'type':'minecraft:biome'}]})
 save(f'data/ashenprotocol/forge/biome_modifier/{ore}.json',{'type':'forge:add_features','biomes':'#minecraft:is_'+dim,'features':ap(ore),'step':'underground_ores'})
for mob,biomes in [('ash_stalker',['minecraft:plains','minecraft:savanna','minecraft:badlands']),('marsh_lurker',['minecraft:swamp','minecraft:mangrove_swamp']),('frost_sentinel',['minecraft:snowy_plains','minecraft:snowy_taiga','minecraft:ice_spikes']),('rift_hound',['minecraft:nether_wastes','minecraft:warped_forest','minecraft:the_end','minecraft:end_highlands'])]:
 save(f'data/ashenprotocol/forge/biome_modifier/spawn_{mob}.json',{'type':'forge:add_spawns','biomes':biomes,'spawners':{'type':ap(mob),'weight':12,'minCount':1,'maxCount':2}})
# Minimal typed NBT writer, preserving block entity loot table metadata.
def utf(s):b=s.encode();return struct.pack('>H',len(b))+b
def payload(t,v):
 if t==3:return struct.pack('>i',v)
 if t==4:return struct.pack('>q',v)
 if t==8:return utf(v)
 if t==9:return bytes([v[0]])+struct.pack('>i',len(v[1]))+b''.join(payload(v[0],a) for a in v[1])
 if t==10:return b''.join(bytes([typ])+utf(k)+payload(typ,val) for k,(typ,val) in v.items())+b'\0'
 raise ValueError(t)
sites=[('ash_outpost','灰烬哨站',['minecraft:plains','minecraft:savanna','minecraft:forest'],'cinder_shrine','minecraft:stone_bricks','raw_ash','archive_chip'),('tidal_archive','潮汐档案馆',['minecraft:swamp','minecraft:mangrove_swamp'],'tide_shrine','minecraft:prismarine_bricks','signal_shard','archive_chip'),('frost_observatory','冰原观测站',['minecraft:snowy_plains','minecraft:snowy_taiga','minecraft:ice_spikes'],None,'minecraft:packed_ice','frost_lens','signal_shard'),('sunken_workshop','沙海工坊',['minecraft:desert','minecraft:badlands'],None,'minecraft:cut_sandstone','sun_disk','raw_ash'),('rift_foundry','裂隙铸造所',['minecraft:nether_wastes','minecraft:warped_forest','minecraft:crimson_forest'],'rift_shrine','minecraft:polished_blackstone_bricks','memory_shard','rift_dust'),('last_archive','终末档案库',['minecraft:end_highlands','minecraft:end_midlands'],'atlas_shrine','minecraft:purpur_block','void_shard','woven_echo')]
for index,(id,name,biomes,shrine,stone,drop,extra) in enumerate(sites):
 palette=[];palette_lookup={};coords={}
 def place(x,y,z,name,props=None,nbt=None):
  key=(name,json.dumps(props,sort_keys=True))
  if key not in palette_lookup:
   entry={'Name':(8,name)}
   if props:entry['Properties']=(10,{k:(8,str(v)) for k,v in props.items()})
   palette_lookup[key]=len(palette);palette.append(entry)
  b={'pos':(9,(3,[x,y,z])),'state':(3,palette_lookup[key])}
  if nbt:b['nbt']=(10,nbt)
  coords[(x,y,z)]=b
 # Authored layouts: inner courtyard plus different shapes of wings, towers, colonnades.
 for x in range(19):
  for z in range(19):
   for y in range(1,9):place(x,y,z,'minecraft:air')
   place(x,0,z,stone if (x+z)%3 else ap('archive_bricks'))
 for x,z in [(1,1),(1,17),(17,1),(17,17),(5,5),(5,13),(13,5),(13,13)]:
  height=5+((x+z+index)%3)
  for y in range(1,height):place(x,y,z,stone)
  place(x,height,z,'minecraft:sea_lantern' if index%2 else 'minecraft:glowstone')
 for x in range(2,17):
  for z in range(2,17):
   if index in (0,3) and (z in (2,16)) and x%4:place(x,1,z,stone);place(x,2,z,stone)
   if index in (1,4) and (x in (2,16)) and z%4:place(x,1,z,stone);place(x,2,z,stone)
   if index in (2,5) and abs(x-9)+abs(z-9) in (6,7):place(x,1,z,stone)
 # Two side galleries; no spawning mobs inside template so peaceful worldgen is safe.
 for x in range(3,7):
  for z in range(6,13):
   if index%2:place(z,4,x,stone)
   else:place(x,4,z,stone)
 if shrine:place(9,1,9,ap(shrine))
 else:place(9,1,9,ap('signal_glass'))
 for x,z in [(4,9),(14,9)]:place(x,1,z,'minecraft:chest',{'facing':'south','type':'single','waterlogged':'false'},{'id':(8,'minecraft:chest'),'LootTable':(8,ap('chests/'+id))})
 data={'DataVersion':(3,3465),'size':(9,(3,[19,10,19])),'palette':(9,(10,palette)),'blocks':(9,(10,list(coords.values()))),'entities':(9,(10,[]))}
 p=R/f'data/ashenprotocol/structures/{id}.nbt';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(gzip.compress(b'\x0a\0\0'+payload(10,data),mtime=0))
 save(f'data/ashenprotocol/worldgen/template_pool/{id}.json',{'name':ap(id),'fallback':'minecraft:empty','elements':[{'weight':1,'element':{'element_type':'minecraft:single_pool_element','location':ap(id),'processors':'minecraft:empty','projection':'rigid'}}]})
 save(f'data/ashenprotocol/tags/worldgen/biome/has_{id}.json',{'replace':False,'values':biomes})
 structure={'type':'minecraft:jigsaw','biomes':'#'+ap('has_'+id),'step':'surface_structures','spawn_overrides':{},'terrain_adaptation':'beard_thin','start_pool':ap(id),'size':1,'start_height':{'absolute':48 if index==4 else 0},'max_distance_from_center':80,'use_expansion_hack':False}
 if index!=4:structure['project_start_to_heightmap']='WORLD_SURFACE_WG'
 save(f'data/ashenprotocol/worldgen/structure/{id}.json',structure)
 save(f'data/ashenprotocol/worldgen/structure_set/{id}.json',{'structures':[{'structure':ap(id),'weight':1}],'placement':{'type':'minecraft:random_spread','spacing':20,'separation':8,'salt':714320+index*117}})
 save(f'data/ashenprotocol/tags/worldgen/structure/{id}.json',{'replace':False,'values':[ap(id)]})
 save(f'data/ashenprotocol/loot_tables/chests/{id}.json',{'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':3,'max':5},'entries':[{'type':'minecraft:item','name':ap(a),'weight':weight,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':maxcount}}]} for a,weight,maxcount in [(drop,4,4),(extra,3,3),('protocol_fragment',3,5)]]},{'rolls':1,'entries':[{'type':'minecraft:item','name':'minecraft:bread','functions':[{'function':'minecraft:set_count','count':4}]}]}]})
# Recipe book unlocks and visible advancement tree with biome/boss/production achievements.
for path in (R/'data/ashenprotocol/recipes').glob('*.json'):
 data=json.loads(path.read_text());ingredients=list(data.get('key',{}).values()) or data.get('ingredients',[])
 if not ingredients:continue
 first=ingredients[0]['item']
 save(f'data/ashenprotocol/advancements/recipes/{path.stem}.json',{'parent':'minecraft:recipes/root','criteria':{'ingredient':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':[first]}]}}},'rewards':{'recipes':[ap(path.stem)]}})
save('data/ashenprotocol/advancements/expedition/root.json',{'display':{'icon':item('archive_compass'),'title':{'text':'失落坐标'},'description':{'text':'加工矿石、追踪遗迹，重建自己的协议网络'},'background':'minecraft:textures/block/deepslate.png','show_toast':False,'announce_to_chat':False},'criteria':{'start':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':[ap('raw_ash')]}]}}}})
parent='root'
for id,label in [('ash_ingot','压制余灰'),('archive_compass','追踪档案'),('cinder_core','余烬的回应'),('tide_core','潮汐的回应'),('rift_core','裂隙的回应'),('atlas_core','找回世界坐标')]:
 save(f'data/ashenprotocol/advancements/expedition/{id}.json',{'parent':ap('expedition/'+parent),'display':{'icon':item(id),'title':{'text':label},'description':{'text':'获得'+zh[id]},'frame':'challenge' if id.endswith('core') else 'task'},'criteria':{'obtain':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':[ap(id)]}]}}},'rewards':{'experience':40}});parent=id
print(f'Generated {len(zh)} item/block entries, {len(enemies)} entity skins, 6 ruins, 5 ores, {len(list((R/"data/ashenprotocol/recipes").glob("*.json")))} crafting recipes')
# Original six-region dimension, using Minecraft's noise engine and base vegetation assets.
regions=[('ash_steppe','余灰荒原',0xb89683,0x705451,0xc9a193,0x7d6155,1.0,0.2,'ash_stalker'),('signal_grove','信号林地',0x60cfc2,0x3d8294,0x93bcb3,0x3c8876,0.7,0.7,'ash_stalker'),('tide_mire','静潮湿地',0x719aba,0x3c7786,0x829caa,0x4e7961,0.8,0.9,'marsh_lurker'),('frost_expanse','镜霜原野',0xc0dce5,0x6f9fb9,0xd0e4e5,0x98b4b9,-0.6,0.6,'frost_sentinel'),('sun_scar','日轮裂土',0xdfb880,0x907854,0xd9ba98,0xa29359,1.8,0.0,'ash_stalker'),('rift_highlands','失谐高地',0x9c83d2,0x71618d,0xb5a3cc,0x746a99,0.4,0.4,'rift_hound')]
for name,label,sky,water,fog,grass,temp,rain,mob in regions:
 biome={'has_precipitation':rain>0,'temperature':temp,'downfall':rain,'effects':{'sky_color':sky,'water_color':water,'water_fog_color':water,'fog_color':fog,'grass_color':grass,'foliage_color':grass,'mood_sound':{'sound':'minecraft:ambient.cave','tick_delay':6000,'block_search_extent':8,'offset':2}},'spawners':{'monster':[{'type':ap(mob),'weight':30,'minCount':1,'maxCount':3},{'type':'minecraft:skeleton','weight':25,'minCount':1,'maxCount':2}],'creature':[{'type':'minecraft:sheep','weight':10,'minCount':2,'maxCount':4},{'type':'minecraft:pig','weight':8,'minCount':2,'maxCount':3}]},'spawn_costs':{},'carvers':{'air':['minecraft:cave','minecraft:cave_extra_underground','minecraft:canyon']},'features':[[],[],[],[],[],[],[],[],[],[],[]]}
 biome['features'][2]=['minecraft:lake_lava_underground'];biome['features'][6]=['minecraft:ore_dirt','minecraft:ore_gravel','minecraft:ore_iron_upper','minecraft:ore_iron_middle','minecraft:ore_coal_upper','minecraft:ore_redstone','minecraft:ore_diamond','minecraft:ore_gold']
 biome['features'][9]=['minecraft:patch_grass_plain','minecraft:trees_plains','minecraft:flower_default'] if name in ('signal_grove','tide_mire') else ['minecraft:patch_grass_plain']
 biome['features'][10]=['minecraft:freeze_top_layer']
 save(f'data/ashenprotocol/worldgen/biome/{name}.json',biome)
 for lang in ['zh_cn','en_us']:
  p=f'assets/ashenprotocol/lang/{lang}.json';d=json.loads((R/p).read_text());d['biome.ashenprotocol.'+name]=label if lang=='zh_cn' else name.replace('_',' ').title();save(p,d)
save('data/ashenprotocol/dimension_type/lost_coordinates.json',{'ultrawarm':False,'natural':True,'piglin_safe':False,'respawn_anchor_works':False,'bed_works':False,'has_raids':False,'has_skylight':True,'has_ceiling':False,'coordinate_scale':1.0,'ambient_light':0.05,'min_y':-64,'height':384,'logical_height':384,'infiniburn':'#minecraft:infiniburn_overworld','effects':'minecraft:overworld','monster_spawn_block_light_limit':0,'monster_spawn_light_level':{'type':'minecraft:uniform','value':{'min_inclusive':0,'max_inclusive':7}}})
save('data/ashenprotocol/dimension/lost_coordinates.json',{'type':ap('lost_coordinates'),'generator':{'type':'minecraft:noise','settings':'minecraft:overworld','biome_source':{'type':'minecraft:multi_noise','biomes':[{'biome':ap(region[0]),'parameters':{'temperature':temp,'humidity':humidity,'continentalness':[-1,1],'erosion':[-1,1],'depth':[-1,1],'weirdness':[-1,1],'offset':0}} for region,temp,humidity in zip(regions,[-.9,-.5,-.1,.3,.65,.95],[-.5,.5,.8,-.7,-.3,.3])]}}})
save('data/minecraft/tags/worldgen/biome/is_overworld.json',{'replace':False,'values':[ap(r[0]) for r in regions]})
for site,region in [('ash_outpost','ash_steppe'),('tidal_archive','tide_mire'),('frost_observatory','frost_expanse'),('sunken_workshop','sun_scar')]:
 p=f'data/ashenprotocol/tags/worldgen/biome/has_{site}.json';d=json.loads((R/p).read_text());d['values'].append(ap(region));save(p,d)
shaped('coordinate_gate',['ASA','FCF','AAA'],{'A':'signal_ingot','S':'cinder_core','F':'archive_chip','C':'minecraft:ender_pearl'})
for lang in ['zh_cn','en_us']:
 p=f'assets/ashenprotocol/lang/{lang}.json';d=json.loads((R/p).read_text());d['item.ashenprotocol.coordinate_gate']='坐标门' if lang=='zh_cn' else 'Coordinate Gate';save(p,d)
texture('assets/ashenprotocol/textures/item/coordinate_gate.png',16,16,lambda x,y: (151,125,233,255) if 3<x<12 and 1<y<14 and (x in (4,11) or y in (2,13)) else (0,0,0,0))
save('assets/ashenprotocol/models/item/coordinate_gate.json',{'parent':'minecraft:item/generated','textures':{'layer0':ap('item/coordinate_gate')}})
print('Generated original lost-coordinates dimension with six biomes and craftable return gate')

save("data/ashenprotocol/advancements/recipes/coordinate_gate.json", {"parent":"minecraft:recipes/root","criteria":{"ingredient":{"trigger":"minecraft:inventory_changed","conditions":{"items":[{"items":[ap("signal_ingot")]}]}}},"rewards":{"recipes":[ap("coordinate_gate")]}})

# End biome labels can cover empty void columns: custom structure codec rejects low/void starts.
save('data/ashenprotocol/worldgen/structure/last_archive.json', {'type':ap('island_archive'),'biomes':'#'+ap('has_last_archive'),'step':'surface_structures','spawn_overrides':{},'terrain_adaptation':'beard_thin','start_pool':ap('last_archive')})
