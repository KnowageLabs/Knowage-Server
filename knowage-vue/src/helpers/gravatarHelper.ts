import md5 from 'crypto-js/md5'

export function getGravatar(email:string, size?:number) : string{

    let MD5=md5(email.toLowerCase())
    const picSize = size || 80;

    return 'http://www.gravatar.com/avatar/' + MD5 + '.jpg?d=retro&s=' + picSize;
}