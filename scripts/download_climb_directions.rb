require 'open-uri'

ids = 2198
act = 584
s = ""
(ids-act).times{|x|
	nr = x+1+act
	data = lambda {open("http://narzew.org/bikeheaven/api/climbs/get_directions_request.php?id=#{nr}"){|s| return s.read }}.call
	data = data.split("^api^")[1]
	File.open("climb_directions/txt/request#{nr}.txt","wb"){|w| w.write(data) }
	print "\nnarzew.org: Request #{nr} complete.\n"
	system("curl -o \"climb_directions/json/climb#{nr}.json\" \"#{data}&key=YOUR_API_KEY\"")
	#open(ata){|gapi| File.open("climb_directions/json/climb#{nr}.json","wb"){|w| w.write(gapi.read) }}
	print "\ngoogle api: Request #{nr} complete.\n"
}
print s
