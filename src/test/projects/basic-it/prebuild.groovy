File dotSvnDir = new File(basedir, 'dotSvnDir')
assert dotSvnDir.exists()
assert dotSvnDir.isDirectory()
assert dotSvnDir.renameTo(new File(basedir, '.svn')) 
