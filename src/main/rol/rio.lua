-- ROLMETA {"version":1,"package":"rol","dependencies":[],"functions":[{"name":"print","mangled":"rol_print6fcfbb5b9d10","args":[{"type":"dyn?"}],"returnType":"<nothing>"},{"name":"readLine","mangled":"rol_readLine6ed808","args":[],"returnType":"String"},{"name":"readNumber","mangled":"rol_readNumber_6c966","args":[],"returnType":"Number"}],"variables":[],"classes":[],"interfaces":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
function rol_print6fcfbb5b9d10(s)
if s == nil then
    print("null")
else
    print(s)
end
end
function rol_readLine6ed808()
return io.read("*l")
end
function rol_readNumber_6c966()
return io.read("*n")
end