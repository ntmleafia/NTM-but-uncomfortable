package com.hbm.leafialib.math;
// SI Unit prefixes utility I made on Lua for use in OpenComputers translated to Java.
// Here's original code if you also play OpenComputers
/*
local sipfx = {}
sipfx.siPrefix = {"k","M","G","T","P","E","Z","Y","R","Q"}
sipfx.siPrefixFull = {"Kilo","Mega","Giga","Terra","Peta","Exa","Zetta","Yotta","Ronna","Quetta"}
function sipfx.round(num,decimals)
  local pow2 = 10^decimals
  return math.floor(num*pow2+0.5)/pow2
end
function sipfx.auto(x)
  local pfxLv = math.floor(math.log(x,1000))
  return (pfxLv == 0 and tostring(x).." ") or tostring(sipfx.round(x/1000^pfxLv,2)).." "..sipfx.siPrefix[pfxLv]
end
return sipfx
 */
public class SiPfx { // Yeah i know SI is supposed to be both uppercase. SHUT UP - leafinia
	public static final String[] siPrefix = new String[]{"k","M","G","T","P","E","Z","Y","R","Q"};
	public static final String[] siPrefixFull = new String[]{"Kilo","Mega","Giga","Terra","Peta","Exa","Zetta","Yotta","Ronna","Quetta"};
	public static double round(double num,int decimals) {
		double pow2 = Math.pow(10,decimals);
		return Math.floor(num * pow2 + 0.5) / pow2;
	}
	public static String auto(double x) {
		byte pfxLv = (byte)Math.floor(Math.log(x)/Math.log(1000));
		return (pfxLv == 0 ? String.valueOf(x)+" " : String.valueOf(round(Math.pow(x/1000,pfxLv),2))+" ")+siPrefix[pfxLv-1];
	}
}
