package com.example.yin.service.impl;

import com.example.yin.dao.CommentMapper;
import com.example.yin.domain.Comment;
import com.example.yin.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService{
    @Autowired
    private CommentMapper commentMapper;

    /**
     * 添加评论
     * @param comment 评论信息
     * @return Boolean
     */
    @Override
    public boolean addComment(Comment comment) {
        return commentMapper.insertSelective(comment) > 0 ? true:false;
    }

    /**
     * 更新评论信息
     * @param comment 评论信息
     * @return Boolean
     */
    @Override
    public boolean updateCommentMsg(Comment comment) {
        return commentMapper.updateCommentMsg(comment) >0 ?true:false;
    }

    /**
     * 删除评论
     * @param id  评论id
     * @return Boolean
     */
    @Override
    public boolean deleteComment(Integer id) {
        return commentMapper.deleteComment(id) >0 ?true:false;
    }

    /**
     * 当前歌曲的评论
     * @param songId 歌曲id
     * @return List
     */
    @Override
    public List<Comment> commentOfSongId(Integer songId)

    {
        return commentMapper.commentOfSongId(songId);
    }

    /**
     * 歌曲列表评论
     * @param songListId 歌曲列表id
     * @return List
     */
    @Override
    public List<Comment> commentOfSongListId(Integer songListId)

    {
        return commentMapper.commentOfSongListId(songListId);
    }
}
