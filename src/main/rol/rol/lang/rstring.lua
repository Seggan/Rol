-- ROLMETA {"version":1,"package":"rol.lang","functions":[{"name":"subs","mangled":"subs_rol_lang_cf0c6_74b90s_cf0c673starti25395a_35323endi25395a2f92ee","args":[{"name":"s","type":"String"},{"name":"starti","type":"Num"},{"name":"endi","type":"Num"}],"returnType":"String"}],"variables":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function subs_rol_lang_cf0c6_74b90s_cf0c673starti25395a_35323endi25395a2f92ee(s, starti, endi)

    return string.sub(s, starti + 1, endi)
end