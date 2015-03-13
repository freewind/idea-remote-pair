How to init project

First Step: clone two repository

    git clone git@github.com:freewind/IdeaRemotePair.git
    git clone git@github.com:freewind/remote-pair-server.git
    
Second Step: 

1. File -> New Project -> IntelliJ Platform Plugin -> Choose "Scala" -> Choose Project SDK : IDEA IU/IC (New -> Select directory where IntelliJ installed)
2. remove netty-all-5.0.0.Alpha1.jar from Project SDK : IDEA IU/IC
3. Choose Project Path which point to where you clone IdeaRemotePair to local
4. Add all jar in lib folder to classpath
5. Run as a plugin, Let's Rock!