-- ROLMETA {"version":1,"package":"rol.lang","functions":[{"name":"print","mangled":"rol_lang_print6fcfb77e7ddcs5b9cc373","args":[{"name":"s","type":"dyn"}],"returnType":"<nothing>"},{"name":"readNumber","mangled":"rol_lang_readNumber481f1e_5cef6","args":[],"returnType":"Num"},{"name":"readLine","mangled":"rol_lang_readLine6ed803a8dfbc","args":[],"returnType":"String"}],"variables":[],"structs":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function rol_lang_print6fcfb77e7ddcs5b9cc373(s)
if s == nil then
    print("null")
else
    print(s)
end
end
function rol_lang_readLine6ed803a8dfbc()
return io.read("*l")
end
function rol_lang_readNumber481f1e_5cef6()
return io.read("*n")
end