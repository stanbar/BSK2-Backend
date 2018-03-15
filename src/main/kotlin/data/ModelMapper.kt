package data

interface ModelMapper<Entity, Object>{
    fun fromEntity(entity: Entity) : Object
    fun toEntity(`object`: Object) : Entity
}