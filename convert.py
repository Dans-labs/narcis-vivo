import md5

def go(filePath, column):
	f = open(filePath, 'r')
	out = open('/tmp/out', 'w')
	head = True
	for line in f:
		if head:
			out.write(line)
			head = False
		else:
			blocks = line.split('\t')
			m = md5.new()
			blocks[column] = blocks[column].replace('"','')
			m.update(blocks[column])
			blocks[column] = '"%s"' % m.hexdigest()
			l = '\t'.join(blocks)
			out.write(l)
	out.close()
	
if __name__ == '__main__':
	go('/home/cgueret/Documents/Projects/DANS/Vivo/data/csv/_vivo_pub.csv.bak', 8)
	#go('/home/cgueret/Documents/Projects/DANS/Vivo/data/csv/_vivo_pub_authors.csv.bak', 1)
	