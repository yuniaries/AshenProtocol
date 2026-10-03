"""Build a self-contained CurseForge-format PCL ZIP from this project's own JAR."""
from pathlib import Path
import json,zipfile,hashlib,tomllib,io,struct,argparse,subprocess
ROOT=Path(__file__).resolve().parents[1]
props=dict(line.split('=',1) for line in (ROOT/'gradle.properties').read_text().splitlines() if '=' in line and not line.startswith('#'))
version=props['mod_version'];launcher=ROOT/'distribution/PCL/Plain Craft Launcher 2.exe'
if not launcher.exists():launcher=ROOT.parent/'PCL/Plain Craft Launcher 2.exe'
assert launcher.exists(), 'Missing bundled PCL binary'
jar=ROOT/f'build/libs/ashenprotocol-{version}.jar'
p=argparse.ArgumentParser();p.add_argument('--output',type=Path,default=ROOT/'build/release');args=p.parse_args();args.output.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(jar) as j:
 assert j.testzip() is None
 assert not any('RuntimeProbe' in n or 'ClientSmoke' in n for n in j.namelist())
 assert struct.unpack('>H',j.read('dev/yuni/ashenprotocol/AshenProtocol.class')[6:8])[0]==61
 manifest_mod=tomllib.loads(j.read('META-INF/mods.toml').decode())
 assert {d['modId'] for d in manifest_mod['dependencies']['ashenprotocol']}=={'forge','minecraft'}
 assert manifest_mod['mods'][0]['displayURL']=='https://github.com/yuniaries/AshenProtocol'
 for n in j.namelist():
  if n.endswith('.json'):json.loads(j.read(n))
manifest={'minecraft':{'version':props['minecraft_version'],'modLoaders':[{'id':'forge-'+props['forge_version'],'primary':True}]},'manifestType':'minecraftModpack','manifestVersion':1,'name':'灰烬协议：回声网络 · 自研原生版 '+version,'version':version,'author':'yuniaries','overrides':'overrides','files':[]}
readme='''灰烬协议：回声网络 — yuniaries 自研原生版
已有PCL：把整个ZIP拖入PCL，安装为新版本后启动、新建世界。
没有PCL：解压本ZIP，运行启动PCL.bat或PCL目录内的exe，再把下载的同一ZIP拖入启动器安装。
首次安装需要联网；Java、Minecraft、Forge与运行库由启动器检测或下载，本包不是离线游戏本体。
Minecraft 1.20.1 / Forge 47.3.22 / Java 17（64位），建议分配3–4GB内存。
PCL首次安装需联网下载Minecraft和Forge；自研MOD已内置，无需手工补依赖。
菜单“GitHub 主页”打开 https://github.com/yuniaries，“项目仓库”打开 https://github.com/yuniaries/AshenProtocol 。
教程键为J，避免与原版P键社交界面冲突。源码和文档在ZIP的source目录。
进入世界领取手册，右键或按J打开八章任务和服务器同步的状态面板。
本包不是落幕曲衍生版，不包含其MOD清单、任务、菜单、资源、作者按钮或存档。
自主玩法：碎片→熵表→中继→手枪→死亡回声→熵晶体→净化器→世界重建。
辅助第三方MOD可自行添加，但当前发布版的核心玩法不依赖任何第三方玩法MOD。
不要把旧衍生版的mods和config覆盖进来。旧版本继续保留，迁移存档前自行备份。
更多配方、任务条件、局限和开发来源参见仓库README与docs。
'''
name=f'灰烬协议_自研原生版_{version}_拖入PCL.zip';out=args.output/name
with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as z:
 for rel in subprocess.check_output(['git','ls-files','-z'],cwd=ROOT).decode().split('\0'):
  if rel and (ROOT/rel).is_file() and not rel.startswith('distribution/PCL/'):z.write(ROOT/rel,'source/'+rel)
 for file in launcher.parent.iterdir():
  if file.is_file():z.write(file,'PCL/'+file.name)
 z.writestr('启动PCL.bat', b'@echo off\r\nstart "" "%~dp0PCL\\Plain Craft Launcher 2.exe"\r\n')
 z.writestr('manifest.json',json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
 z.writestr('overrides/mods/'+jar.name,jar.read_bytes())
 z.writestr('overrides/options.txt','lang:zh_cn\nguiScale:2\nrenderDistance:10\nsimulationDistance:6\nmaxFps:120\nautoJump:false\n')
 z.writestr('安装与玩法说明.txt',readme)
 z.writestr('overrides/灰烬协议玩法说明.txt',readme)
with zipfile.ZipFile(out) as z:
 assert z.testzip() is None
 assert [n for n in z.namelist() if n.startswith('overrides/mods/') and n.endswith('.jar')]==['overrides/mods/'+jar.name]
 assert z.read('overrides/mods/'+jar.name)==jar.read_bytes()
 assert z.read('PCL/Plain Craft Launcher 2.exe')==launcher.read_bytes()
 assert 'source/tools/package.py' in z.namelist()
 assert z.read('启动PCL.bat') == b'@echo off\r\nstart "" "%~dp0PCL\\Plain Craft Launcher 2.exe"\r\n'
(args.output/'SHA256SUMS.txt').write_text(hashlib.sha256(out.read_bytes()).hexdigest()+'  '+name+'\n')
(args.output/'安装与玩法说明.txt').write_text(readme)
print(out,'bytes',out.stat().st_size)
