package cn.violin.home.book.service;

import cn.violin.home.book.entity.BlogSeq;
import cn.violin.home.book.entity.BookmarkSeq;
import cn.violin.home.book.dao.BlogSeqRepo;
import cn.violin.home.book.dao.BlogTypeSeqRepo;
import cn.violin.home.book.dao.BookmarkSeqRepo;
import cn.violin.home.book.entity.BlogTypeSeq;
import cn.violin.home.book.entity.BookmarkType;
import cn.violin.home.book.utils.NumberEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NumberService {

    @Autowired
    private BookmarkSeqRepo bookmarkSeqRepo;
    @Autowired
    private BlogSeqRepo blogSeqRepo;
    @Autowired
    private BlogTypeSeqRepo blogTypeSeqRepo;

    @Transactional
    public String getNumberId(NumberEnum table) {

        String numberId = null;
        switch (table.value()) {

            case "t_bookmark":
                BookmarkSeq seq1 = new BookmarkSeq();
                numberId = String.format("%014d", bookmarkSeqRepo.save(seq1).getBkSeqId());
                break;
            case "t_blog":
                BlogSeq seq2 = new BlogSeq();
                numberId = String.format("%014d", blogSeqRepo.save(seq2).getBlogSeqId());
                break;
            case "t_blog_type":
                BlogTypeSeq seq3 = new BlogTypeSeq();
                numberId = String.format("%014d", blogTypeSeqRepo.save(seq3).getBlogTypeSeqId());

        }

        return numberId;
    }


}
