"""Original deterministic pixel textures. Uses only the Python standard library."""
from pathlib import Path
import json,struct,zlib
r=Path(__file__).resolve().parents[1]/'src/main/resources'
def png(name,block=False):
 colors={'protocol_fragment':(70,224,209),'entropy_crystal':(158,112,217),'echo_residue':(223,173,91),'entropy_meter':(70,224,209),'field_guide':(70,224,209),'entropy_sink':(158,112,217),'relay_off':(43,91,97),'relay_on':(70,224,209),'pistol_metal':(118,143,154),'pistol_grip':(25,38,52),'pistol_signal':(70,224,209)}
 dark=(25,38,52);metal=(118,143,154);c=colors[name];pixels=[]
 for y in range(16):
  row=[]
  for x in range(16):
   v=None
   if block:
    v=dark
    if name.startswith('pistol'):v=c if (x+y)%5 else tuple(max(0,n-12) for n in c)
    elif x in (1,14) or y in (1,14):v=metal
    elif 4<=x<=11 and 4<=y<=11:v=c if x in (7,8) or x in (4,11) or y in (4,11) else dark
   elif name in ('protocol_fragment','entropy_crystal','echo_residue'):
    if 2<=y<14 and abs(x-8)<max(2,5-abs(7-y)//2):v=c if x<8 else tuple(int(n*.65) for n in c)
    if x==6 and 4<=y<10:v=(204,226,220)
   else:
    if 3<=x<=12 and 2<=y<=13:v=metal
    if 4<=x<=11 and 3<=y<=12:v=dark
    if x in (7,8) and 5<=y<=10:v=c
    if name=='field_guide':
     v=None
     if 2<=x<=12 and 2<=y<=13:v=(100,70,49)
     if 4<=x<=12 and 3<=y<=11:v=(30,63,67)
     if x==3 and 3<=y<=12:v=(174,117,62)
     if 4<=x<=12 and y in (12,13):v=(230,220,185)
     if (x in (7,10) and 5<=y<=9) or (y in (5,9) and 7<=x<=10):v=c
     if x==11 and y==14:v=(199,109,71)
    if name=='entropy_sink' and (x in (5,10) or y in (4,11)) and 4<=x<=11 and 3<=y<=12:v=c
   row.extend((*v,255) if v else (0,0,0,0))
  pixels.append(b'\0'+bytes(row))
 def chunk(n,d):return struct.pack('>I',len(d))+n+d+struct.pack('>I',zlib.crc32(n+d)&0xffffffff)
 folder='block' if block else 'item';p=r/f'assets/ashenprotocol/textures/{folder}/{name}.png';p.parent.mkdir(parents=True,exist_ok=True)
 p.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(b''.join(pixels)))+chunk(b'IEND',b''))
def save(path,data): (r/path).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
for name in ['protocol_fragment','entropy_crystal','echo_residue','entropy_meter','field_guide','entropy_sink']:
 png(name);save(f'assets/ashenprotocol/models/item/{name}.json',{'parent':'minecraft:item/generated','textures':{'layer0':f'ashenprotocol:item/{name}'}})
for name in ['relay_off','relay_on','pistol_metal','pistol_grip','pistol_signal']:png(name,True)
for state in ['off','on']:save(f'assets/ashenprotocol/models/block/protocol_relay_{state}.json',{'parent':'minecraft:block/cube_all','textures':{'all':f'ashenprotocol:block/relay_{state}'}})
p=r/'assets/ashenprotocol/models/item/phase_pistol.json';m=json.loads(p.read_text());m['textures']={'metal':'ashenprotocol:block/pistol_metal','grip':'ashenprotocol:block/pistol_grip','signal':'ashenprotocol:block/pistol_signal','particle':'ashenprotocol:block/pistol_metal'};save('assets/ashenprotocol/models/item/phase_pistol.json',m)
recipes={'entropy_sink':{'type':'minecraft:crafting_shaped','pattern':['IMI','FCF','IRI'],'key':{'I':{'item':'minecraft:iron_ingot'},'M':{'item':'ashenprotocol:entropy_meter'},'F':{'item':'ashenprotocol:protocol_fragment'},'C':{'item':'ashenprotocol:entropy_crystal'},'R':{'item':'minecraft:redstone'}},'result':{'item':'ashenprotocol:entropy_sink'}},'echo_recycling':{'type':'minecraft:crafting_shapeless','ingredients':[{'item':'ashenprotocol:echo_residue'}],'result':{'item':'ashenprotocol:protocol_fragment','count':4}}}
for name,data in recipes.items():
 save(f'data/ashenprotocol/recipes/{name}.json',data)
 item='entropy_crystal' if name=='entropy_sink' else 'echo_residue'
 save(f'data/ashenprotocol/advancements/recipes/{name}.json',{'parent':'minecraft:recipes/root','criteria':{'ingredient':{'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':[f'ashenprotocol:{item}']}]}}},'requirements':[['ingredient']],'rewards':{'recipes':[f'ashenprotocol:{name}']}})
for lang in ['zh_cn','en_us']:
 path=f'assets/ashenprotocol/lang/{lang}.json';d=json.loads((r/path).read_text());d.update({'item.ashenprotocol.entropy_sink':'熵债净化器' if lang=='zh_cn' else 'Entropy Sink','key.ashenprotocol.journal':'打开协议任务书' if lang=='zh_cn' else 'Open Quest Book','key.categories.ashenprotocol':'灰烬协议' if lang=='zh_cn' else 'Ashen Protocol'});save(path,d)
print('Generated 11 original textures, 2 recipes and recipe unlocks')

for lang in ['zh_cn','en_us']:
 path=f'assets/ashenprotocol/lang/{lang}.json';d=json.loads((r/path).read_text());d['item.ashenprotocol.field_guide']='协议任务书' if lang=='zh_cn' else 'Protocol Quest Book';save(path,d)
