#encoding: utf-8
$places = []
$act_folder = ""
# Actual ID of the folder; default 0;
$act_id = 0
# ID of the author
# 1 - Narzew
# 2 - A.G.
# More reserved for others
$author_id = 1
# ID of the area
# 1 - Lubelskie (PL)
# 2 - Podkarpackie (PL)
# 3 - Podlaskie (PL)
$area_id = 1
data = File.read('KMLData.kml', :encoding=>"UTF-8")
# Split by unparsed folders
folders_unparsed = data.split("<folder>")[1..-1]
folders_unparsed.each{|folder|
	$act_folder = folder.split("<name>")[1].split("</name>")[0].strip
	$act_id += 1
	
	places_unparsed = data.split("Placemark")
	# Skip first element
	places_unparsed = places_unparsed[1..-1]
	places_unparsed.each{|x|
		next unless x.include?("<name>") && x.include?("<coordinates>")
		name = x.split("<name>")[1].split("</name>")[0].strip
		description = lambda {
			return x.split("<description>")[1].split("</description>")[0]
		}.call rescue description = ""
		if description.include?("<br/>")
			description = description.split("<br/>")[-1]
		end
		if description.include?("<br>")
			description = description.split("<br>")[-1]
		end
		if description.include?("<img src")
			description = ""
		end
		description = description.gsub(" ]]>","").gsub("]]>","").gsub(" ","\x20")
		description = description.strip
		description = description.gsub("\'","\\\'")
		coordinates = x.split("<coordinates>")[1].split("</coordinates>")[0].strip
		a = coordinates.split(",")
		point_x = a[0].strip
		point_y = a[1].strip
		$places << [name,description,point_x,point_y]
	}
}
s = "insert into places(name,description,lat,lng,cat,area,active,author) values "
$places.each{|x|
	s << "(\'#{x[0]}\', \'#{x[1]}\', #{x[2]}, #{x[3]}, #{$act_id}, #{$area_id}, 1, #{$author_id}),\n"
}
s[-2] = ";"
File.open("result.sql","wb"){|w| w.write(s) }
