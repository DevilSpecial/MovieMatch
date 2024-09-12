package com.moviematch.Rooms

data class Room(
val hostId: String?=null,
val password: String?=null,
val guestId:String?=null,
val genre_id : List<String>
){
    constructor(hostId: String?, password: String) : this(hostId, password, null, emptyList())

}
