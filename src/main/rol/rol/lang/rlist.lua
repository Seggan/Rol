-- ROLMETA {"version":1,"package":"rol.lang","functions":[{"name":"set","mangled":"rol_lang_set6fcfb74f3fd6list_8117232b09eindex481f1e5fb28dvalue5b9cc36ac917","args":[{"name":"list","type":"rol.lang/DynamicList"},{"name":"index","type":"Num"},{"name":"value","type":"dyn?"}],"returnType":"<nothing>"},{"name":"get","mangled":"rol_lang_get5b9cc34f3fa8list_8117232b09eindex481f1e5fb28d","args":[{"name":"list","type":"rol.lang/DynamicList"},{"name":"index","type":"Num"}],"returnType":"dyn"}],"variables":[],"structs":[{"name":"rol.lang/DynamicList","fields":[],"const":false}]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function rol_lang_get5b9cc34f3fa8list_8117232b09eindex481f1e5fb28d(list, index)
return list[index]
end
function rol_lang_set6fcfb74f3fd6list_8117232b09eindex481f1e5fb28dvalue5b9cc36ac917(list, index, value)
list[index] = value
end