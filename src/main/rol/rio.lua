-- ROLMETA {"version":1,"package":"rol","dependencies":[],"variables":[],"classes":[],"interfaces":[]}
package.path = "./?.lua;" .. package.path
require "rol_core"
local rol_print_32113
rol_print_32113 = function (x)
        if x == nil then
            print("null")
        else
            print(x)
        end
end
local rol_readLine_32113
rol_readLine_32113 = function ()
return assertNonNull( io.read("*l") , "file 'rio.rol', line 14, column 11, statement 'extern { io.read(\"*l\") }!'")
end
local rol_readNumber_32113
rol_readNumber_32113 = function ()
return assertNonNull( io.read("*n") , "file 'rio.rol', line 18, column 11, statement 'extern { io.read(\"*n\") }!'")
end