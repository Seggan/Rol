-- ROLMETA {"version":1,"package":"rol.lang","functions":[{"name":"toString","mangled":"rol_lang_toString6ed803_2ba27x5b9cc378","args":[{"name":"x","type":"dyn"}],"returnType":"String"},{"name":"toNumber","mangled":"rol_lang_toNumber481f1e_341efx6ed80378","args":[{"name":"x","type":"String"}],"returnType":"Num"}],"variables":[],"structs":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function rol_lang_toString6ed803_2ba27x5b9cc378(x)
if x == nil then
    return "null"
else
    return tostring(x)
end
end
function rol_lang_toNumber481f1e_341efx6ed80378(x)
return tonumber(x)
end