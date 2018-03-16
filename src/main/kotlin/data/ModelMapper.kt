package data

interface ModelMapper<Entity, Object>{
    fun fromEntity(entity: Entity) : Object
    fun toEntity(item: Object) : Entity
}