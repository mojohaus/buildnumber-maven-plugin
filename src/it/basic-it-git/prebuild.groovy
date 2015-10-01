File dotGitDir = new File(basedir, 'dotGitDir')
assert dotGitDir.exists()
assert dotGitDir.isDirectory()
assert dotGitDir.renameTo(new File(basedir, '.git')) 
