function main(n)
	init(n)
	if n.pri<=0 then
		return ''
	end
	local s=split(n.title,' ')
	local b=iif(string.find(s[#s],'^%(%d+.+%)$') == nil, 0, 1)
	local t=''
	for i=1,#s -b do
		t=t..s[i]
	end
	return "QQ,"..t..","..n.desc
end
-----libs below.
function init(n)
	if n.name == nil then n.name = '' end
	if n.ticker == nil then n.ticker = '' end
	if n.title == nil then n.title = '' end
	if n.desc == nil then n.desc = '' end
	if n.pri == nil then n.pri = 0 end
end
function iif(exp,trueRet,falseRet)
	if exp then return trueRet else return falseRet end
end
function split(s, sp)
	local res = {}
	local temp = s
	local len = 0
	while true do
		len = string.find(temp, sp)
		if len ~= nil then
			local result = string.sub(temp, 1, len-1)
			temp = string.sub(temp, len+1)
			table.insert(res, result)
		else
			table.insert(res, temp)
			break
		end
	end
	return res
end
