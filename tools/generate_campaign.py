"""Authored farming and industry assets. Recipes form explicit survival dependencies."""
import re,json,hashlib
from generate_expansion import R,save,ap,shaped,shapeless,texture,payload,gzip
from pathlib import Path
JAVA=R.parent/'java/dev/yuni/ashenprotocol/campaign/CampaignContent.java'
src=JAVA.read_text()
def array(name):return re.findall(r'"([^"]+)"',re.search(r'String\[\] '+name+r'=\{(.*?)\}',src).group(1))
crops=array('CROPS');materials=array('MATERIALS');machines=array('MACHINES');foods=array('FOODS');supplies=array('SUPPLIES');decor=array('DECOR')
labels={}
for group,text in [(crops,'灰麦,信号浆果,潮米,霜根,日椒,回声豆,裂瓜,记忆草'),(materials,'碳粉,碳板,铜导线,信号电路,回声电路,裂隙电路,坐标电路,钢锭,陶瓷板,晶格框架,转子,泵头,滤网,肥沃堆肥,生物浆,生物燃料,营养凝胶,治疗提取物,霜寒提取物,热能提取物,回声纤维,导电纤维,晶体矩阵,记忆矩阵,虚空矩阵,密封档案,相位线圈,导航阵列,复苏之心,生命圈核心,深渊核心,风暴核心,根源核心,曙光核心,深渊钥匙,风暴钥匙,根源钥匙,曙光钥匙,契约凭证,远征封印,深矿碎片,深层锭,风暴晶屑,根系树脂,曙光碎片,谐振锭,锚定模块,太阳能单元,滤水器,精密齿轮,强化板,压力阀'),(machines,'碳化窑,导线拉丝机,电路装配机,炼钢炉,堆肥处理机,生物精炼机,野外厨房,草药提取机,纤维纺织机,矩阵压缩机,档案解码机,精密车床,相位装配机,生命圈复苏器'),(foods,'野外面包,浆果果酱,潮米饭,霜根浓汤,日椒口粮,回声豆汤,裂瓜切片,记忆草茶,远征餐,霜寒口粮,耐热口粮,回声口粮,治疗肉汤,霜根派,潮米糕,回声豆面包,浆果派,腌肉,香辛炖汤,信号沙拉,记忆曲奇,强化口粮,守卫盛宴,曙光盛宴'),(supplies,'野外绷带,复苏药膏,净毒胶囊,耐热胶囊,御寒胶囊,回声胶囊'),(decor,'碳砖,钢铁地砖,陶瓷地砖,电路面板,回声面板,根源砖,曙光砖,深渊砖,风暴砖,记忆地砖,铜格栅,强化玻璃,档案灯,信号灯,根源灯,曙光灯')]:
 names=text.split(',');assert len(group)==len(names),(len(group),len(names));labels.update(zip(group,names))
block_ids=set(machines+decor+['abyss_shrine','storm_shrine','root_shrine','dawn_shrine','contract_terminal','expedition_beacon'])
labels.update(zip(['abyss_shrine','storm_shrine','root_shrine','dawn_shrine','contract_terminal','expedition_beacon','deep_gate','storm_repeater','dawn_projector'],['深渊祭坛','风暴祭坛','根源祭坛','曙光祭坛','契约终端','远征信标','深层坐标门','风暴连射器','曙光投射器']))
for prefix,name in [('steel','钢铁'),('echo','深层回声'),('harmonic','谐振')]:
 for suffix,label in [('blade','刃'),('pickaxe','镐'),('axe','斧'),('shovel','锹'),('helmet','头盔'),('chestplate','胸甲'),('leggings','护腿'),('boots','靴')]:labels[prefix+'_'+suffix]=name+label
for c in crops:labels[c+'_seeds']=labels[c]+'种子'
for name in labels:
 col=(70+int(hashlib.sha256(name.encode()).hexdigest()[:2],16)%150,150,190)
 isblock=name in block_ids
 def pixels(x,y,col=col,name=name,isblock=isblock):
  if not isblock and not (2<x<13 and 2<y<14):return (0,0,0,0)
  if name in crops:return (*(col if (x+y)%3 else (89,164,72)),255)
  return (*(col if (x+y)%7<2 else (43,57,69) if isblock else (164,187,191)),255)
 texture('assets/ashenprotocol/textures/'+('block/' if isblock else 'item/')+name+'.png',16,16,pixels)
 if isblock:
  save(f'assets/ashenprotocol/blockstates/{name}.json',{'variants':{'':{'model':ap('block/'+name)}}})
  save(f'assets/ashenprotocol/models/block/{name}.json',{'parent':'minecraft:block/cube_all','textures':{'all':ap('block/'+name)}})
  save(f'assets/ashenprotocol/models/item/{name}.json',{'parent':ap('block/'+name)})
  save(f'data/ashenprotocol/loot_tables/blocks/{name}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':ap(name)}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 else:save(f'assets/ashenprotocol/models/item/{name}.json',{'parent':'minecraft:item/handheld' if name.endswith(('_blade','_pickaxe','_axe','_shovel')) else 'minecraft:item/generated','textures':{'layer0':ap('item/'+name)}})
for i,c in enumerate(crops):
 states={}
 for age in range(8):
  h=3+age;col=(130+i*12,175-age*5,65+i*8)
  texture(f'assets/ashenprotocol/textures/block/{c}_{age}.png',16,16,lambda x,y,h=h,col=col: (*col,255) if y>=16-h and (x in (3,7,11) or (x+y)%7==0) else (0,0,0,0))
  save(f'assets/ashenprotocol/models/block/{c}_{age}.json',{'parent':'minecraft:block/crop','textures':{'crop':ap(f'block/{c}_{age}')}})
  states['age='+str(age)]={'model':ap(f'block/{c}_{age}')}
 save(f'assets/ashenprotocol/blockstates/{c}_crop.json',{'variants':states})
 mature={'condition':'minecraft:block_state_property','block':ap(c+'_crop'),'properties':{'age':'7'}}
 save(f'data/ashenprotocol/loot_tables/blocks/{c}_crop.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':ap(c)}],'conditions':[mature]},{'rolls':1,'entries':[{'type':'minecraft:item','name':ap(c+'_seeds')}]},{'rolls':1,'entries':[{'type':'minecraft:item','name':ap(c+'_seeds'),'functions':[{'function':'minecraft:apply_bonus','enchantment':'minecraft:fortune','formula':'minecraft:binomial_with_bonus_count','parameters':{'extra':3,'probability':.5714286}}]}],'conditions':[mature]}]})
 shapeless(c+'_seeds',['minecraft:wheat_seeds','protocol_fragment',['minecraft:wheat','minecraft:sweet_berries','minecraft:kelp','minecraft:potato','minecraft:carrot','minecraft:cocoa_beans','minecraft:melon_slice','minecraft:dandelion'][i]],count=2)
for prefix,mat,core in [('steel','steel_ingot','signal_circuit'),('echo','deep_ingot','abyss_core'),('harmonic','harmonic_ingot','dawn_core')]:
 for suffix,pattern in [('blade',[' C ',' I ',' S ']),('pickaxe',['ICI',' S ',' S ']),('axe',['IC ','IS ',' S ']),('shovel',[' C ',' S ',' S ']),('helmet',['ICI','I I']),('chestplate',['I I','ICI','III']),('leggings',['ICI','I I','I I']),('boots',['I I','C I'])]:
  keys={'I':mat,'C':core,'S':'minecraft:stick'};shaped(prefix+'_'+suffix,pattern,{k:v for k,v in keys.items() if any(k in row for row in pattern)})
 for layer in (1,2):texture(f'assets/ashenprotocol/textures/models/armor/{prefix}_layer_{layer}.png',64,32,lambda x,y: (87,174,188,255) if (x+y)%9<2 else (71,83,104,255))
for i,m in enumerate(machines):
 mat='ash_ingot' if i<2 or m=='steel_foundry' else 'steel_ingot' if i<10 else 'deep_ingot'
 center=['minecraft:furnace','minecraft:piston','minecraft:comparator','minecraft:blast_furnace','minecraft:composter','minecraft:brewing_stand','minecraft:smoker','minecraft:brewing_stand','minecraft:loom','minecraft:piston','archive_chip','rotor','phase_coil','restoration_heart'][i]
 shaped(m,['IPI','FCF','III'],{'I':mat,'P':'protocol_fragment','F':'copper_wire' if i>1 else 'protocol_fragment','C':center})
craft={
'carbon_plate':['carbon_dust']*4,'ceramic_plate':['minecraft:brick']*4,'lattice_frame':['reinforced_plate','copper_wire','ceramic_plate'],
'rotor':['steel_ingot','copper_wire','minecraft:iron_nugget'],'pump_head':['steel_ingot','minecraft:bucket','pressure_valve'],
'filter_mesh':['copper_wire','minecraft:string'],'bio_pulp':['echo_bean','ash_wheat'],'nutrient_gel':['bio_fuel','root_resin'],
'frost_extract':['frost_root','minecraft:glass_bottle'],'thermal_extract':['sun_pepper','minecraft:glass_bottle'],
'conductive_thread':['echo_fiber','copper_wire'],'crystal_matrix':['signal_ingot','entropy_crystal','carbon_plate'],
'void_matrix':['void_shard','memory_matrix','echo_circuit'],'echo_circuit':['signal_circuit','woven_echo'],
'rift_circuit':['echo_circuit','rift_ingot','memory_shard'],'atlas_circuit':['rift_circuit','atlas_core','void_shard'],
'sealed_archive':['archive_chip','memory_shard','minecraft:paper'],'phase_coil':['rift_circuit','conductive_thread','rift_ingot'],
'navigation_array':['phase_coil','atlas_circuit','sun_disk','frost_lens','solar_cell','anchor_module','lattice_frame'],'restoration_heart':['restoration_cell','memory_matrix','bio_fuel','water_filter','precision_gear'],
'biosphere_core':['restoration_heart','abyss_core','storm_core','root_core','dawn_core','navigation_array'],
'abyss_key':['atlas_core','memory_matrix','deep_ingot'],'storm_key':['abyss_core','phase_coil','storm_shard'],
'root_key':['storm_core','root_resin','healing_extract'],'dawn_key':['root_core','navigation_array','dawn_fragment'],
'expedition_seal':['rift_core','signal_circuit','contract_token'],'anchor_module':['atlas_core','steel_ingot','phase_coil'],
'solar_cell':['sun_disk','signal_circuit'],'water_filter':['filter_mesh','pump_head'],'reinforced_plate':['steel_ingot','carbon_plate'],
'pressure_valve':['steel_ingot','minecraft:iron_nugget'],'deep_gate':['atlas_core','navigation_array','memory_matrix'],
'storm_repeater':['abyss_core','storm_shard','steel_ingot','phase_coil'],'dawn_projector':['dawn_core','harmonic_ingot','navigation_array'],
'field_bandage':['minecraft:paper','memory_herb'],'restoration_salve':['healing_extract','nutrient_gel'],
'purity_capsule':['healing_extract','minecraft:milk_bucket'],'thermal_capsule':['thermal_extract','minecraft:glass_bottle'],
'frost_capsule':['frost_extract','minecraft:glass_bottle'],'echo_capsule':['memory_herb','echo_fiber'],
'contract_terminal':['signal_circuit','ash_ingot','minecraft:paper'],'expedition_beacon':['rift_circuit','restoration_cell','steel_ingot']}
for name,ingredients in craft.items():shapeless(name,ingredients,count=2 if name in supplies else 1)
save('data/ashenprotocol/recipes/deep_ingot.json',{'type':'minecraft:smelting','ingredient':{'item':ap('deep_ore_fragment')},'result':ap('deep_ingot'),'experience':1.0,'cookingtime':200})
for i,f in enumerate(foods):shapeless(f,[crops[i%8],crops[(i+3)%8], 'minecraft:bowl' if 'soup' in f or 'stew' in f or 'broth' in f else 'minecraft:wheat']+(['healing_extract'] if i>=20 else []))
for i,d in enumerate(decor):shaped(d,['MM','MM'],{'M':['carbon_plate','steel_ingot','ceramic_plate','signal_circuit','woven_echo','root_resin','dawn_fragment','deep_ingot','storm_shard','memory_shard','copper_wire','minecraft:glass','archive_chip','signal_shard','root_resin','dawn_fragment'][i]},count=8)
newmobs=['deep_scavenger','storm_drone','root_keeper','dawn_raider','abyss_cantor','storm_sovereign','root_matriarch','dawn_regent']
for i,mob in enumerate(newmobs):
 labels[mob+'_spawn_egg']=['深层拾荒者','风暴无人机','根系守护者','曙光掠夺者','深渊咏者','风暴君王','根源母体','曙光执政官'][i]+'刷怪蛋'
 save(f'assets/ashenprotocol/models/item/{mob}_spawn_egg.json',{'parent':'minecraft:item/template_spawn_egg'})
 texture(f'assets/ashenprotocol/textures/entity/{mob}.png',64,64,lambda x,y,i=i:(80+i*15,155-i*10,195,255) if (x*3+y*(i+1))%13<3 else (25,32,48,255))
 drop=['deep_ore_fragment','storm_shard','root_resin','dawn_fragment','abyss_core','storm_core','root_core','dawn_core'][i]
 save(f'data/ashenprotocol/loot_tables/entities/{mob}.json',{'type':'minecraft:entity','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':ap(drop),'functions':[{'function':'minecraft:set_count','count':1 if i>=4 else {'type':'minecraft:uniform','min':2,'max':4}}]}]}]})
for lang in ('zh_cn','en_us'):
 p=f'assets/ashenprotocol/lang/{lang}.json';d=json.loads((R/p).read_text())
 for name,label in labels.items():d[('block.' if name in block_ids else 'item.')+'ashenprotocol.'+name]=label if lang=='zh_cn' else name.replace('_',' ').title()
 for i,mob in enumerate(newmobs):d['entity.ashenprotocol.'+mob]=labels[mob+'_spawn_egg'].removesuffix('刷怪蛋') if lang=='zh_cn' else mob.replace('_',' ').title()
 save(p,d)
print('Campaign farming, multi-input industrial dependencies, consumables and late combat assets generated.')
# Six different environmental regions share the vanilla terrain engine but use authored ecology and structures.
regions=[('abyss_shore','深渊黑岸','deep_scavenger',0x242948,0x182941,0x314664),('storm_plateau','风暴台地','storm_drone',0x535b78,0x2c5878,0x74939b),('root_canopy','根源密林','root_keeper',0x263d34,0x376951,0x41743f),('dawn_wastes','曙光废土','dawn_raider',0xa18660,0x55727a,0x918650),('memory_fen','记忆沼地','deep_scavenger',0x435365,0x364b70,0x576b69),('harmonic_ridge','谐振山脊','storm_drone',0x63567d,0x465674,0x688589)]
for i,(name,label,mob,fog,water,grass) in enumerate(regions):
 biome=json.loads((R/'data/ashenprotocol/worldgen/biome/signal_grove.json').read_text())
 biome['effects'].update(sky_color=fog,fog_color=fog,water_color=water,water_fog_color=water,grass_color=grass,foliage_color=grass)
 biome['temperature']=.6 if i!=1 else -.3;biome['downfall']=.8 if i in (2,4) else .3
 biome['spawners']['monster']=[{'type':ap(mob),'weight':35,'minCount':1,'maxCount':2}]
 biome['features'][9]=['minecraft:trees_plains','minecraft:patch_grass_plain','minecraft:flower_default'] if i==2 else ['minecraft:patch_grass_plain']
 save(f'data/ashenprotocol/worldgen/biome/{name}.json',biome)
 for lang in ('zh_cn','en_us'):
  p=f'assets/ashenprotocol/lang/{lang}.json';d=json.loads((R/p).read_text());d['biome.ashenprotocol.'+name]=label if lang=='zh_cn' else name.replace('_',' ').title();save(p,d)
dim=json.loads((R/'data/ashenprotocol/dimension_type/lost_coordinates.json').read_text());dim.update(has_skylight=False,effects='minecraft:the_end',ambient_light=.12)
save('data/ashenprotocol/dimension_type/echo_depths.json',dim)
save('data/ashenprotocol/dimension/echo_depths.json',{'type':ap('echo_depths'),'generator':{'type':'minecraft:noise','settings':'minecraft:overworld','biome_source':{'type':'minecraft:multi_noise','biomes':[{'biome':ap(r[0]),'parameters':{'temperature':t,'humidity':h,'continentalness':[-1,1],'erosion':[-1,1],'depth':[-1,1],'weirdness':[-1,1],'offset':0}} for r,t,h in zip(regions,[-.9,-.5,-.1,.3,.65,.95],[-.5,.5,.8,-.7,-.3,.3])]}}})
p='data/minecraft/tags/worldgen/biome/is_overworld.json';tag=json.loads((R/p).read_text());tag['values']+= [ap(r[0]) for r in regions];save(p,tag)
# Locations serve different survival roles rather than just renaming one copied ruin.
locations=[
 ('growers_depot','培育者补给站',['minecraft:plains',ap('ash_steppe')],None,'carbon_bricks','ash_wheat_seeds','fertile_compost','farm'),
 ('signal_exchange','信号交易所',['minecraft:forest',ap('signal_grove')],None,'circuit_panel','contract_token','signal_circuit','market'),
 ('storm_observatory','风暴观测所',['minecraft:windswept_hills',ap('rift_highlands')],None,'storm_bricks','storm_shard','solar_cell','tower'),
 ('root_sanctuary','根系养护站',['minecraft:swamp',ap('tide_mire')],None,'root_bricks','root_resin','memory_herb_seeds','farm'),
 ('abyss_cathedral','深渊大教堂',[ap('abyss_shore')],'abyss_shrine','abyss_bricks','deep_ore_fragment','sealed_archive','hall'),
 ('storm_citadel','风暴堡垒',[ap('storm_plateau')],'storm_shrine','storm_bricks','storm_shard','phase_coil','fort'),
 ('root_vault','根源藏库',[ap('root_canopy')],'root_shrine','root_bricks','root_resin','nutrient_gel','garden'),
 ('dawn_spire','曙光高塔',[ap('dawn_wastes')],'dawn_shrine','dawn_bricks','dawn_fragment','reinforced_plate','tower'),
 ('memory_lab','记忆实验室',[ap('memory_fen')],None,'memory_tiles','sealed_archive','memory_shard','lab'),
 ('echo_quarry','回声采掘站',[ap('harmonic_ridge')],None,'steel_tiles','deep_ore_fragment','echo_fiber','quarry'),
 ('nether_pumpworks','下界泵站',['minecraft:nether_wastes','minecraft:basalt_deltas'],None,'steel_tiles','pressure_valve','memory_shard','pipes'),
 ('ender_foundry','终界装配厂',['minecraft:end_highlands','minecraft:end_midlands'],None,'ceramic_tiles','void_matrix','dawn_fragment','lab')]
for index,(id,name,biomes,shrine,stone,drop,extra,style) in enumerate(locations):
 palette=[];lookup={};coords={};size=29 if style!='tower' else 23;height=18 if style=='tower' else 12;center=size//2
 def place(x,y,z,name,props=None,nbt=None):
  if not(0<=x<size and 0<=z<size and 0<=y<height):return
  key=(name,json.dumps(props,sort_keys=True))
  if key not in lookup:
   entry={'Name':(8,name)}
   if props:entry['Properties']=(10,{k:(8,str(v)) for k,v in props.items()})
   lookup[key]=len(palette);palette.append(entry)
  b={'pos':(9,(3,[x,y,z])),'state':(3,lookup[key])}
  if nbt:b['nbt']=(10,nbt)
  coords[x,y,z]=b
 for x in range(size):
  for z in range(size):
   place(x,0,z,ap(stone))
   for y in range(1,height):place(x,y,z,'minecraft:air')
 # Outer walls, four distinct wings, safe three-block entry, interior walkable streets.
 for x in range(1,size-1):
  for z in range(1,size-1):
   boundary=x in (1,size-2) or z in (1,size-2)
   room=(x in (4,10,size-11,size-5) and (4<=z<=10 or size-11<=z<=size-5)) or (z in (4,10,size-11,size-5) and (4<=x<=10 or size-11<=x<=size-5))
   if boundary and not(abs(x-center)<=1 and z==1):
    for y in range(1,4):place(x,y,z,ap(stone))
   if room:
    for y in range(1,5):place(x,y,z,ap(stone) if y!=3 else ap('reinforced_glass'))
 # Architecture driven by functional site: greenhouses, vertical observatories, laboratory benches, fortress battlements.
 if style in ('farm','garden'):
  for x in range(5,size-5):
   for z in range(5,size-5):
    if abs(x-center)>2 and abs(z-center)>2:
     if x%4==0:place(x,0,z,'minecraft:water',{'level':'0'})
     else:place(x,0,z,'minecraft:farmland',{'moisture':'7'});place(x,1,z,ap(crops[(x+z+index)%8]+'_crop'),{'age':'7'})
 elif style=='tower':
  for y in range(1,height-2):
   for x,z in [(center-3,center-3),(center+3,center-3),(center-3,center+3),(center+3,center+3)]:place(x,y,z,ap(stone))
   place(center-3,y,center,'minecraft:ladder',{'facing':'east','waterlogged':'false'});place(center-4,y,center,ap(stone))
  for y in (5,10,15):
   for x in range(center-3,center+4):
    for z in range(center-3,center+4):
     if not(x==center-3 and z==center):place(x,y,z,ap(stone))
 elif style=='market':
  for x in (6,size-7):
   for z in range(5,size-5,5):place(x,1,z,ap('contract_terminal'));place(x,3,z,ap('signal_lamp'))
 elif style=='quarry':
  for x in range(5,size-5,3):
   for z in range(5,size-5,3):place(x,1,z,'minecraft:deepslate');place(x,2,z,ap('memory_ore'))
 elif style in ('lab','pipes'):
  for x in range(5,size-5,5):
   for z in (6,size-7):place(x,1,z,ap('archive_decoder' if style=='lab' else 'wire_drawer'));place(x,2,z,ap('signal_lamp'))
 elif style=='fort':
  for x in range(2,size-2,2):
   for z in (2,size-3):place(x,4,z,ap(stone))
 else:
  for x in (center-4,center+4):
   for z in range(4,size-4,4):
    for y in range(1,7):place(x,y,z,ap(stone))
 for x,z in [(3,3),(3,size-4),(size-4,3),(size-4,size-4)]:place(x,4,z,ap('archive_lamp'))
 if shrine:place(center,1,center,ap(shrine))
 for x,z in [(3,center),(size-4,center),(center,size-4)]:place(x,1,z,'minecraft:chest',{'facing':'south','type':'single','waterlogged':'false'},{'id':(8,'minecraft:chest'),'LootTable':(8,ap('chests/'+id))})
 data={'DataVersion':(3,3465),'size':(9,(3,[size,height,size])),'palette':(9,(10,palette)),'blocks':(9,(10,list(coords.values()))),'entities':(9,(10,[]))}
 p=R/f'data/ashenprotocol/structures/{id}.nbt';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(gzip.compress(b'\x0a\0\0'+payload(10,data),mtime=0))
 save(f'data/ashenprotocol/worldgen/template_pool/{id}.json',{'name':ap(id),'fallback':'minecraft:empty','elements':[{'weight':1,'element':{'element_type':'minecraft:single_pool_element','location':ap(id),'processors':'minecraft:empty','projection':'rigid'}}]})
 save(f'data/ashenprotocol/tags/worldgen/biome/has_{id}.json',{'replace':False,'values':biomes})
 if index==11:structure={'type':ap('island_archive'),'biomes':'#'+ap('has_'+id),'step':'surface_structures','spawn_overrides':{},'terrain_adaptation':'beard_thin','start_pool':ap(id)}
 else:structure={'type':'minecraft:jigsaw','biomes':'#'+ap('has_'+id),'step':'surface_structures','spawn_overrides':{},'terrain_adaptation':'beard_thin','start_pool':ap(id),'size':1,'start_height':{'absolute':48 if index==10 else 0},'max_distance_from_center':80,'use_expansion_hack':False}
 if index not in (10,11):structure['project_start_to_heightmap']='WORLD_SURFACE_WG'
 save(f'data/ashenprotocol/worldgen/structure/{id}.json',structure)
 save(f'data/ashenprotocol/worldgen/structure_set/{id}.json',{'structures':[{'structure':ap(id),'weight':1}],'placement':{'type':'minecraft:random_spread','spacing':22,'separation':9,'salt':921731+index*139}})
 save(f'data/ashenprotocol/tags/worldgen/structure/{id}.json',{'replace':False,'values':[ap(id)]})
 save(f'data/ashenprotocol/loot_tables/chests/{id}.json',{'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':4,'max':6},'entries':[{'type':'minecraft:item','name':ap(a),'weight':w,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':1,'max':n}}]} for a,w,n in [(drop,5,5),(extra,3,3),('protocol_fragment',4,8),(crops[index%8]+'_seeds',2,3)]]}]})
for path in (R/'data/ashenprotocol/recipes').glob('*.json'):
 recipe=json.loads(path.read_text());ingredients=recipe.get('ingredients') or list(recipe.get('key',{}).values()) or [recipe['ingredient']]
 entry=ingredients[0]['item'];save('data/ashenprotocol/advancements/recipes/'+path.name,{'parent':'minecraft:recipes/root','criteria':{'ingredient':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':[entry]}]}}},'rewards':{'recipes':[ap(path.stem)]}})
# All registered machines and crops have mining/harvest tags; new shrines require no privileged command.
for tag,values in [('mineable/pickaxe',list(block_ids)),('needs_iron_tool',machines)]:
 p=f'data/minecraft/tags/blocks/{tag}.json';d=json.loads((R/p).read_text()) if (R/p).exists() else {'replace':False,'values':[]};d['values']=sorted(set(d['values']+[ap(v) for v in values]));save(p,d)
print('Second original realm: six regions; 12 additional functional exploration structures, 18 total.')
# Derive journal instructions from the shipped recipe definitions and production code.
workshop=(JAVA.parent.parent/'expansion/WorkshopEntity.java').read_text()
processing={}
for m,inp,out,count,ticks,secondary,qty in re.findall(r'case "([^"]+)" -> new Recipe\("([^"]+)", "([^"]*)", (\d+), (\d+)(?:, "([^"]+)", (\d+))?\);',workshop):
 processing[m]={'input':inp,'output':out,'count':int(count),'ticks':int(ticks),'secondary':secondary,'secondaryCount':int(qty or 0)}
# New code compact recipes have no spaces; accept both styles.
for m,inp,out,count,ticks,secondary,qty in re.findall(r'case "([^"]+)" -> new Recipe\("([^"]+)",\s*"([^"]*)",\s*(\d+),\s*(\d+)(?:,\s*"([^"]+)",\s*(\d+))?\);',workshop):
 processing[m]={'input':inp,'output':out,'count':int(count),'ticks':int(ticks),'secondary':secondary,'secondaryCount':int(qty or 0)}
assert len(processing)==20,processing
root=R.parents[2]
(root/'docs/PROCESSING.json').write_text(json.dumps(processing,ensure_ascii=False,indent=2)+'\n')
lang=json.loads((R/'assets/ashenprotocol/lang/zh_cn.json').read_text())
vanilla={'coal':'煤炭','iron_ingot':'铁锭','copper_ingot':'铜锭','bone_meal':'骨粉','stick':'木棍','paper':'纸','wheat':'小麦','milk_bucket':'牛奶桶','glass_bottle':'玻璃瓶','ender_pearl':'末影珍珠','nether_star':'下界之星'}
def label(id):
 if ':' not in id:id=ap(id)
 ns,name=id.split(':');return vanilla.get(name,name) if ns=='minecraft' else lang.get('item.'+ns+'.'+name,lang.get('block.'+ns+'.'+name,name))
recipes={}
for p in (R/'data/ashenprotocol/recipes').glob('*.json'):
 recipe=json.loads(p.read_text());result=recipe['result'];id=result if isinstance(result,str) else result['item'];recipes.setdefault(id,[]).append(recipe)
rows=json.loads((root/'docs/campaign-goals.json').read_text());details=[]
special={51:'潜行右键契约终端选择委托，再手持足量材料提交。前四项分别需要8灰烬锭、16灰麦、6信号锭、12信号浆果。每次成功提交奖励3至10凭证和经验，整理冷却60秒。两凭证换4种子。任务要求实际完成一次委托。',61:'在深层领域的深渊大教堂使用深渊钥匙。深渊咏者520生命，蓄力后施加黑暗，近距离受到冲击。掩体和拉开距离能避开技能。准备深层装备、治疗物资、弹药。任务要求亲自击败首领。',66:'在深层领域风暴堡垒使用风暴钥匙。风暴君王620生命，冲击带击退和减速；注意平台边缘。掩体阻断技能视线。获胜掉落风暴核心。',71:'在深层领域根源藏库使用根源钥匙。根源母体720生命，蓄力后缠绕减速并恢复生命。准备持续输出与净毒胶囊；不要只靠低频攻击。获胜掉落根源核心。',75:'在深层领域曙光高塔使用曙光钥匙。曙光执政官900生命，近距离冲击并呼叫曙光掠夺者。处理增援、利用掩体和远程攻击，低于半血技能加速。获胜掉落曙光核心。',77:'把复苏之心、深渊核心、风暴核心、根源核心、曙光核心与导航阵列无序合成为生命圈核心。需要重复挑战取得额外核心，因为装备、钥匙和合成都要消耗材料。生命圈复苏器使用核心加4份生物燃料加工，每20秒完整度+200、熵债-300。',78:'手持远征封印右键远征信标，参加六波挑战。前五波不同敌人，第六波余烬守卫。活动需玩家在48格内保持存活；离开、死亡或掉线取消。主人潜行空手右键可取消。获胜得16凭证、3精密齿轮并返还封印。需要逐波清除敌人，不能站着等待。',79:'亲自击败曙光执政官、完成六波远征，并使世界完整度达到9500、熵债低于200。建设复苏器与中继网络，停止无节制使用高耗能武器。完成后仍可继续建造、探索和重复挑战。'}
special.update({80: '实际完成100次机器加工。机器放置者拥有生产记录；遗迹机器第一次手动使用时确定记录归属。断电、缺料或满仓暂停不会计数。', 81: '用底部漏斗真实取出累计64件成品。手工空手收取不计自动运输；模拟管道检测不会计数。', 82: '亲自破坏32株成熟原创作物。未成熟作物、原版作物和只拿到种子不会计入。', 83: '实际完成10次契约委托，每次提交仍受60秒冷却。可以轮换农作物和矿产委托。', 84: '实际成功使用绷带或复苏药膏10次。满血失败与冷却期间点击不会计入。', 85: '相位装配机完成10次谐振锭制造。需要深层锭与虚空矩阵，产量保存在机器拥有者的记录中。', 86: '生命圈复苏器实际完成一次加工：生命圈核心加4生物燃料，20秒改善世界。仅持有机器不会完成。', 87: '世界坐标锚实际完成一次加工，消耗坐标核心，30秒完整度+500、熵债-1000。', 88: '实际进入回声深层，服务器确认维度后记录；仅制作坐标门不够。', 89: '在失序领域亲自走访全部六种原创群系，按每秒的当前所在地记录，进度保存在玩家数据中。', 90: '在回声深层亲自走访全部六种原创群系。定位器寻找不同据点有助于跨越多个区域。', 91: '完成三次六波远征，胜利返还封印，可再次启动。取消和失败不计入。', 92: '亲自击败八种阶段首领各至少一次。只拾取别人掉落的核心不计入该精通目标。', 93: '累计完成1000次机器加工，可由多台机器并行推进；机器放置者拥有统计记录。这是长期工业目标，不强制阻断主线。', 94: '亲自收获全部八种原创作物的成熟植株，记录每种至少一次。', 95: '完成完整生态复苏后，使完整度达到10000、熵债清零。可以继续生存与建造，后续射击仍会产生新的熵债。'})
for row in rows:
 index=row['index'];id=row['item'];text=special.get(index,'')
 if not text and id:
  text='目标：背包持有'+label(id)+'。'
  if id in crops:text+='种子可用小麦种子、协议碎片和对应原版作物合成。在湿润耕地种植，光照至少9，可使用骨粉。成熟后收获产物与更多种子。'
  elif id in (c+'_seeds' for c in crops):text+='湿润耕地右键种植。'
  for machine,r in processing.items():
   if r['output']==id:text+='使用'+label(machine)+'加工：1 × '+label(r['input'])+(' + '+str(r['secondaryCount'])+' × '+label(r['secondary']) if r['secondary'] else '')+'，'+str(r['ticks']//20)+'秒产出'+str(r['count'])+'份。'
  if ap(id) in recipes:
   rec=recipes[ap(id)][0]
   if rec['type']=='minecraft:smelting':text+='在熔炉烧炼'+label(rec['ingredient']['item'])+'，10秒产出一份。'
   elif 'pattern' in rec:
    text+='工作台各行：'+'；'.join('/'.join('空' if c==' ' else label(rec['key'][c]['item']) for c in line) for line in rec['pattern'])+'。'
   else:text+='无序合成：'+' + '.join(label(x['item']) for x in rec['ingredients'])+'。'
  if id=='deep_gate':text+='仅在主世界开启，抵达深层领域后再次使用返回。不可用床或重生锚。深层领域缺乏自然光，农田须布置照明。'
  if id in machines:text+='接红石信号。顶部输入主材料，侧面输入辅助材料与协议碎片，底部收取输出。每碎片200能量、加工每秒耗1能量，满仓或断电暂停；仅加载区块运行。潜行空手取回两种输入。'
  sources={'deep_ore_fragment':'深层拾荒者掉落，回声采掘站和深渊大教堂的箱子也有。','storm_shard':'风暴无人机掉落，风暴观测所及风暴堡垒箱子也有。','root_resin':'根系守护者掉落，根系养护站及根源藏库箱子也有。','dawn_fragment':'曙光掠夺者掉落，曙光高塔箱子也有。'}
  text+=sources.get(id,'')
  if id.endswith('_core'):text+='对应首领被击败时必定掉落一枚核心，每次召唤需要新钥匙。'
 if not text:text='完成'+row['title']+'，依照前后章节推进。'
 details.append(text)
(JAVA.parent/'CampaignJournal.java').write_text('package dev.yuni.ashenprotocol.campaign;\npublic final class CampaignJournal {\n public static String detail(int chapter){return DETAILS[chapter-24];}\n private static final String[] DETAILS={'+','.join(json.dumps(x,ensure_ascii=False) for x in details)+'};\n}\n')
(root/'docs/CAMPAIGN_GUIDE.md').write_text('# 灰烬协议：生态重启 · 后半程指南\n\n'+''.join('## '+str(row['index']+1)+' · '+row['title']+'\n\n'+text+'\n\n' for row,text in zip(rows,details)))
(root/'docs/CONTENT_CATALOG.json').write_text(json.dumps({'crops':crops,'machines':list(processing),'additionalSites':[x[0] for x in locations],'newRegions':[x[0] for x in regions],'registeredExpansionItems':241,'coreItems':8,'goalCount':96,'recipeCount':len(list((R/'data/ashenprotocol/recipes').glob('*.json')))},ensure_ascii=False,indent=2)+'\n')
