package com.g.estate.service;

import com.g.estate.entity.BlogInfo;
import com.g.estate.entity.BlogType;
import com.g.estate.io.BlogIn;
import com.g.estate.io.BlogTypeIn;
import com.g.estate.utils.Constant;
import com.g.estate.utils.NumberEnum;
import com.g.estate.vo.BlogBoxVo;
import com.g.estate.vo.BlogVo;
import com.mongodb.client.result.UpdateResult;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
//@NoArgsConstructor
@RequiredArgsConstructor
public class BlogInfoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    private final JPAQueryFactory jpaQueryFactory;

    @Autowired
    private NumberService numberService;

    /**
     * bidに指向するドキュメントを取得する
     *
     * @param bid blogユニクロID
     * @return content ドキュメントのコンテント
     */
    public String getContent(String bid) {
        Criteria criteria = Criteria.where("bid").is(bid);
        Query query = Query.query(criteria);
        query.fields().include("content");
        BlogInfo document = mongoTemplate.findOne(query, BlogInfo.class);
        return new String(document.getContent().getData(), StandardCharsets.UTF_8);
    }

    /**
     * ブログ新規作成処理
     *
     * @param input 新規作成に必要情報
     * @return content ドキュメントのコンテント
     */
    @Transactional
    public BlogVo insertContent(BlogIn input) {
        BlogInfo model = new BlogInfo();
        String bid = numberService.getNumberId(NumberEnum.T_BLOG);
        model.setTitle(input.getTitle());
        model.setBtId(input.getBlogTypeId());
        model.setBid(bid);
        model.setContent(new Binary("".getBytes(StandardCharsets.UTF_8)));
        mongoTemplate.save(model);
        BlogVo vo = new BlogVo();
        vo.setBid(bid);
        vo.setTitle(input.getTitle());
        vo.setBid(input.getBlogTypeId());
        vo.setContent("");
        return vo;
    }

    /**
     * ブログ更新処理
     *
     * @param input 更新内容
     * @return long 高新
     */
    public long updateContent(BlogIn input) {
        Criteria criteria = Criteria.where("bid").is(input.getBid());
        Query query = Query.query(criteria);
        Update update = Update.update("bid", input.getBid());
        if (input.getTitle() != null) {
            update = update.set("title", input.getTitle());
        }
        if (input.getContent() != null) {
            update = update.set("content", new Binary(input.getContent().getBytes(StandardCharsets.UTF_8)));
        }
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, BlogInfo.class);
        return updateResult.getMatchedCount();
    }

    /**
     * ブログ削除処理
     *
     * @param bid 削除bid
     * @return void
     */
    public void deleteContent(String bid) {
        Criteria criteria = Criteria.where("bid").is(bid);
        Query query = Query.query(criteria);
        mongoTemplate.remove(query, BlogInfo.class);
    }

    /**
     * ブログ新規作成処理
     *
     * @param input 新規作成に必要情報
     * @return content ドキュメントのコンテント
     */
    @Transactional
    public BlogBoxVo insertBlogType(BlogTypeIn input) {
        BlogType model = new BlogType();
        String btId = numberService.getNumberId(NumberEnum.T_BLOG_TYPE);
        model.setBtName("未分类");
        model.setBtId(btId);
        model.setOwner("xiaoguan");
        model.setUpdateTime("");
        mongoTemplate.save(model);
        BlogIn blogIn = new BlogIn();
        blogIn.setBlogTypeId(btId);
        blogIn.setTitle(LocalDate.now().format(Constant.FORMATTER_DATE));
        BlogVo vo = this.insertContent(blogIn);
        BlogBoxVo box = new BlogBoxVo();
        box.setBtId(btId);
        box.setBtName("未分类");
        List<BlogVo> vos = new ArrayList<>(1);
        vos.add(vo);
        box.setBlogVoList(vos);
        return box;
    }

    /**
     * ブログタイプ修正
     *
     * @param input ブログタイプ更新内容
     */
    @Transactional()
    public void updateBlogTypeName(BlogTypeIn input) {
        Criteria criteria = Criteria.where("btId").is(input.getBtId());
        Query query = Query.query(criteria);
        Update update = Update.update("btId", input.getBtId())
                .set("btName", input.getBtName());

        mongoTemplate.updateFirst(query, update, BlogType.class);
    }

    /**
     * ブログタイプ一覧取得する
     */
    public List<BlogBoxVo> listAll() {
        String owner = "xiaoguan";
        Criteria criteria = Criteria.where("owner").is(owner);
        Query query = Query.query(criteria);
        List<BlogType> docs = mongoTemplate.find(query, BlogType.class);

        if (docs.isEmpty()) {
            BlogTypeIn in = new BlogTypeIn();
            BlogBoxVo box = this.insertBlogType(in);
            List<BlogBoxVo> result = new ArrayList<>(1);
            result.add(box);
            return result;
        } else {
            String btId = docs.stream().findFirst().get().getBtId();
            List<String> btIds = docs.stream().map(BlogType::getBtId).collect(Collectors.toList());
            Criteria c2 = Criteria.where("btId").in(btIds);
            Query q2 = Query.query(c2);
            q2.fields().exclude("content");
            List<BlogInfo> blogs = mongoTemplate.find(q2, BlogInfo.class);
            String bid = blogs.stream().findFirst().get().getBid();
            String content = this.getContent(bid);

            List<BlogBoxVo> result = docs.stream().map(type -> {
                BlogBoxVo box = new BlogBoxVo();
                box.setBtId(type.getBtId());
                box.setBtName(type.getBtName());
                List<BlogVo> blogVos = blogs.stream().filter(doc -> doc.getBtId().equals(type.getBtId())).map(doc -> {
                    BlogVo vo = new BlogVo();
                    vo.setBid(doc.getBid());
                    vo.setTitle(doc.getTitle());
//            vo.setAutoSaveControl();
                    vo.setBtId(doc.getBtId());
                    return vo;
                }).collect(Collectors.toList());
                box.setBlogVoList(blogVos);
                return box;
            }).collect(Collectors.toList());
            result.get(0).getBlogVoList().get(0).setContent(content);

            return result;
        }
    }
}
