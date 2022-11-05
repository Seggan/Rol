-- ROLMETA {"version":1,"package":"rol.lang","functions":[{"name":"subs","mangled":"rol_lang_subs6ed803_6744ds6ed80373starti481f1e_35323endi481f1e2f92ee","args":[{"name":"s","type":"String"},{"name":"starti","type":"Num"},{"name":"endi","type":"Num"}],"returnType":"String"}],"variables":[],"structs":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function rol_lang_subs6ed803_6744ds6ed80373starti481f1e_35323endi481f1e2f92ee(s, starti, endi)
return string.sub(s, starti + 1, endi)
end