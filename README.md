## 👉 I used to hate Git. Today, it saved my project.

I’ll admit it, I’ve always found Git confusing. I only committed my code "for namesake" because I thought I had to. That changed today. \
I ran into a nightmare scenario 😱 --> A Spring Boot starter parent version downgrade (4.0.3 to 3.2.5) triggered a waterfall of annotation errors.  \
IntelliJ stopped recognizing my App.java as the root. I spent hours clearing caches, deleting .idea folders, and tweaking Maven settings based on Gemini AI suggestions. Nothing worked by the way it was gemini who told me to change the version 😑

Frustrated and ready to give up bothh myself 😣 and Gemini 🤖, I realized now I just wanted to go back to when things actually worked. 

The lesson: I finally looked into how to actually "revert" using Git. I discovered that what I thought was a complex tool is actually a massive safety net. For the first time, Git wasn't a chore—it was the "undo" button I desperately needed. \
📢 _If you’re avoiding Git because it feels like extra work, take it from me: You’re not just managing code --> you’re buying insurance for your sanity._


🔑 What i learned:
	

1. `git log --oneline`
--> this will display all the commits I have done with a short hash key for each commit 
2. `git revert <commit hash>`
  * What it does: Creates a new commit that undoes the changes introduced by a previous commit.
  * History: Preserves commit history; the original bad commit remains in the log and the revert is recorded as a corrective commit. 
  * Safe for shared branches: Use on main/master or any branch others may have pulled. 
    `git reset --hard <commit hash>`
  * What it does: Moves your branch pointer to a specified commit and resets the working tree and index to match that commit, discarding commits and uncommitted changes that came after it. 
  * History: Removes commits from the current branch history (locally). 
  * Risk on shared branches: Dangerous if others have pulled the commits you remove requires force-pushing to update remote. 
4. `git clean -fd`
  * -f = force; -d = remove untracked directories 
  * What it does: Removes untracked files and untracked directories only. It will not remove changes inside a file that Git already tracks. To discard changes inside a tracked file, use a reset/restore command instead. 
Best use: when you want to remove the untracked files themselves. 

## ✅ When to use which
* Use **git revert** when: the bad commit is already pushed and other people may have based work on it; you want a safe, auditable undo. 
* Use **git reset** --hard when: you are certain you want to discard local commits and uncommitted changes, and you either are the only one working on the branch or you coordinate with teammates before force-pushing. 
