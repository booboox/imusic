package com.example.yin.service.impl;

import com.example.yin.dao.CollectMapper;
import com.example.yin.domain.Collect;
import com.example.yin.service.CollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectServiceImpl implements CollectService {
    @Autowired
    private CollectMapper collectMapper;

    /**
     * 添加收藏
     * @param collect 收藏歌曲信息
     * @return Boolean
     */
    @Override
    public boolean addCollection(Collect collect) {
        return collectMapper.insertSelective(collect) > 0 ? true : false;
    }

    /**
     * 已收藏的歌曲
     * @param userId 用户id
     * @param songId 收藏的歌曲id
     * @return Boolean
     */
    @Override
    public boolean existSongId(Integer userId, Integer songId) {
        return collectMapper.existSongId(userId, songId) > 0 ? true : false;
    }

    /**
     * 取消收藏
     * @param userId 用户id
     * @param songId 歌曲id
     * @return Boolean
     */
    @Override
    public boolean deleteCollect(Integer userId, Integer songId) {
        return collectMapper.deleteCollect(userId, songId) > 0 ? true : false;
    }

    /**
     * 用户已收藏的所有歌曲
     * @param userId 用户id
     * @return List
     */
    @Override
    public List<Collect> collectionOfUser(Integer userId)

    {
        return collectMapper.collectionOfUser(userId);
    }
}
