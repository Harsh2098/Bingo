# Bingo
Bingo : A 5 Way Game - Android Multiplayer Game

Technologies Used: GRPC + Protocol Buffers, Firebase (In-game Chat), Google Cloud (Hosting Server)

Languages: Kotlin, Java, XML, Protobuf

Detailed Explanation:- https://www.youtube.com/watch?v=1NAUuKmal1A&t=

There are 2 modules app (Android App) and server module. Server is hosted on GCP - Google Cloud Platform

To generate java classes (files) for example BingoActionServiceImplBase,
1. You need to define proto files in src/main/proto
2. Configure gradle files and add necessary proto compiler plugins
3. Then, rebuild the project and newly generated files should appear in build/generated/source/proto folder within each module

Don't forget to specify source set {...} in gradle files so that Android Studio knows where to look for generated files.

Demo repo link:-
https://github.com/Harsh2098/Android-Multiplayer-Tutorial

Learn how to write .proto files:-
https://www.youtube.com/redirect?q=https%3A%2F%2Fdevelopers.google.com%2Fprotocol-buffers%2Fdocs%2Fproto3&v=1NAUuKmal1A&event=video_description&redir_token=R26Q8wvi8WEd9MXGAMK8v_GKv9h8MTU1MjQ2NDMzNUAxNTUyMzc3OTM1

You'll need to remove debug.java... in app level gradle file to generate signed APKs

Bingo: 5 Line Game Play Store Link:-
https://play.google.com/store/apps/details?id=com.hmproductions.bingo

I couldn't find any good resources to start with GRPC on Android, even docs are not that great. I made Youtube video (linked above) to help others.
