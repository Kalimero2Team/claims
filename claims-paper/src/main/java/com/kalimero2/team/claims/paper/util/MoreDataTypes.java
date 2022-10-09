package com.kalimero2.team.claims.paper.util;

import com.jeff_media.morepersistentdatatypes.DataType;
import com.jeff_media.morepersistentdatatypes.datatypes.collections.MapDataType;
import com.jeff_media.morepersistentdatatypes.datatypes.serializable.ConfigurationSerializableArrayDataType;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;
import java.util.UUID;


public interface MoreDataTypes {
    PersistentDataType<?, UUID[]> UUID_ARRAY = DataType.asArray(new UUID[0], DataType.UUID);
    MapDataType<Map<String, String>, String, String> CHUNK_PROPERTY_MAP = DataType.asMap(DataType.STRING, DataType.STRING);
    PersistentDataType<byte[], SerializableChunk[]> SERIALIZABLE_CHUNK_ARRAY = new ConfigurationSerializableArrayDataType<>(SerializableChunk[].class);

}
