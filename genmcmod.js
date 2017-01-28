var inf = "modules.json"
  , outf = "src/main/resources/mcmod.info"
  , fs = require("fs");

var modules = JSON.parse(fs.readFileSync(inf, "UTF-8")).modules;
var mcmod = Object.keys(modules).map(function(k) {
	var v = modules[k];
	var o = {
		"modid": "charset" + k.toLowerCase(),
		"name": v.char,
		"version": "${version}",
		"mcversion": "${mcversion}",
		"description": v.description,
		"authorList": [ "asiekierka" ],
		"url": "http://charset.asie.pl",
		"updateUrl": "http://charset.asie.pl/update.json",
		"logoFile": "assets/charsetlib/textures/items/icon.png"
	};
	return o;
});

fs.writeFileSync(outf, JSON.stringify({
	"modListVersion": 2,
	"modList": mcmod
}, null, 4), "UTF-8");
