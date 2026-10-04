"""Check actual recipe dependencies in a staged survival playthrough, including realm access."""
from pathlib import Path
import json
root=Path(__file__).resolve().parents[1];r=root/'src/main/resources/data/ashenprotocol';ap=lambda s:s if ':' in s else 'ashenprotocol:'+s
recipes=[json.loads(p.read_text()) for p in (r/'recipes').glob('*.json')];machines=json.loads((root/'docs/PROCESSING.json').read_text());goals=json.loads((root/'docs/campaign-goals.json').read_text());catalog=json.loads((root/'docs/CONTENT_CATALOG.json').read_text())
available={ap(s) for s in ['raw_ash','signal_shard','rift_dust','memory_shard','void_shard','echo_residue','frost_lens']};rounds=[]
bosses=[('cinder_key','cinder_core'),('tide_key','tide_core'),('rift_key','rift_core'),('atlas_key','atlas_core'),('abyss_key','abyss_core'),('storm_key','storm_core'),('root_key','root_core'),('dawn_key','dawn_core')]
def ready(id):return id.startswith('minecraft:') or id in available
def add(id):
 if not id:return False
 id=ap(id)
 if id in available:return False
 available.add(id);return True
for turn in range(100):
 before=set(available)
 for rec in recipes:
  ingredients=rec.get('ingredients') or list(rec.get('key',{}).values()) or [rec['ingredient']]
  if all(ready(x['item']) for x in ingredients):
   out=rec['result'];add(out if isinstance(out,str) else out['item'])
 for name in catalog['crops']:
  if ready(ap(name+'_seeds')):add(name)
 for name,v in machines.items():
  if ready(ap(name)) and ready(ap(v['input'])) and (not v['secondary'] or ready(ap(v['secondary']))):add(v['output'])
 for i,(key,core) in enumerate(bosses):
  if ready(ap(key)) and (i<4 or ready(ap('deep_gate'))):add(core)
 if ready(ap('deep_gate')):
  for x in ['deep_ore_fragment','storm_shard','root_resin','dawn_fragment']:add(x)
 if ready(ap('contract_terminal')) and ready(ap('ash_ingot')):add('contract_token')
 gained=available-before
 if not gained:break
 rounds.append(sorted(gained))
required=[ap(x) for x in catalog['machines']+catalog['crops']+['biosphere_core','harmonic_chestplate','dawn_projector','expedition_seal']]+[ap(x['item']) for x in goals if x['item']]
missing=sorted(set(required)-available)
report={'passes':not missing,'reachableOriginalItems':len(available),'dependencyStages':len(rounds),'missing':missing,'stages':rounds,'assumptions':['Vanilla resources, Nether and End are obtainable through normal Minecraft survival.','Original ores and death echoes provide baseline materials.','Boss drops unlock only after their crafted key; second-realm enemies require deep_gate.','Quantity, combat difficulty and inventory logistics are separately checked in runtime tests.']}
(root/'docs/SURVIVAL_AUDIT.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
assert not missing,'Unreachable survival goals: '+', '.join(missing)
print(f'SURVIVAL_ROUTE_PASS: {len(available)} original products reachable through {len(rounds)} dependency stages; final campaign products reachable.')
