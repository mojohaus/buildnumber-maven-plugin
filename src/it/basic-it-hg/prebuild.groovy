File dotHgDir = new File(basedir, 'dotHgDir')
assert dotHgDir.exists()
assert dotHgDir.isDirectory()
assert dotHgDir.renameTo(new File(basedir, '.hg')) 
