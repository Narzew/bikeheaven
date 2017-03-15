require 'find'
s = ""
Find.find('json').each{|x|
	if File.size(x) < 1000
		print "#{x} malfunctioned.\n"
		a = x.gsub("json","txt").gsub("climb","request")
		b = File.read(a).split("&waypoints")[0]+"&units=metric&mode=driving&language=en"
		s = "curl -o \"#{x}\" \"#{b}\"\n#{s}"
	end
}
File.open("malfunctions.sh","wb"){|w| w.write(s) }
